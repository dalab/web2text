# $Id: PrepareHTML.pm 364 2008-02-21 21:08:07Z michal $

package Victor::PrepareHTML;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&prepare_html);

use HTML::Parser;
use HTML::Entities;
use Victor::Cfg;
use Victor::Common;

our $do_img_alt = 1;

sub prepare_html {
	my ($in, $out) = @_;
	my $p = HTML::Parser->new(api_version => 3,
			start_h => [\&_start_element,
				"self,tagname,skipped_text,attr,attrseq"],
			end_h => [\&_end_element, "self,tagname,skipped_text"],
			comment_h => [""]
	);
	# don't decode attribute values
	$p->attr_encoded(1);
	$p->utf8_mode(0);
	# let us handle <tags /> manually
	$p->empty_element_tags(0);
	# stack of deleted / hidden tags
	$p->{deleted_tags} = [];
	$p->{hidden_tags} = [];
	$p->{span_counter} = 0;
	$p->{in_body} = 0;
	$p->{in_pre} = 0;
	# if parsing a partially annotated html, this is set to the class
	# inside annotated blocks
	$p->{existing_class} = "";
	my $need_close = 0;
	if ($out) {
		if (ref($out) eq "GLOB") {
			$p->{out} = $out;
		} else {
			open(my $fh, ">", $out) || die "can't open $out: $!\n";
			$p->{out} = $fh;
			$need_close = 1;
		}
	} else {
		$p->{out} = \*STDOUT;
	}
	if (ref($in) eq "GLOB") {
		$p->parse_file($in) or die $!;
	} else {
		$p->parse($in) or die $!;
	}
	if ($need_close) {
		close($p->{out});
	}
}

sub _start_element {
	my ($self, $name, $skipped_text, $attribs, $attrseq) = @_;
	_anytag($self, $skipped_text);
	if (cfg_contains('tag-delete', $name)) {
		if (!$attribs->{"/"}) {
			# only add to the stack if the tag will have
			# a matching closing tag
			push(@{$self->{deleted_tags}}, $name);
		}
		return;
	}
	if (!_can_print($self)) {
		return;
	}
	if ($name eq "pre") {
		$self->{in_pre}++;
	}
	my $empty_tag = 0;  # <tag />
	my $close_empty_tag = 0; # rewrite <tag /> as <tag></tag>
	# FIXME: this should be configurable
	if ($do_img_alt && $name eq "img" && $attribs->{alt}) {
		$self->{insert_text} = $attribs->{alt};
		# make the extracted alt text appear as child of <img>
		$close_empty_tag = 1;
	}
	if (cfg_isset('hide-ignored-tags') &&
			cfg_contains('tag-ignore', $name)) {
		push(@{$self->{hidden_tags}}, $name);
		if ($attrseq->[$#{$attrseq}] eq "/") {
			$empty_tag = 1;
		}	
		goto out;
	}
	# existing annotation
	if ($name eq "span" && exists($attribs->{id}) &&
			$attribs->{id} =~ /^victor_an[0-9]+$/ &&
			exists($attribs->{class}) &&
			$attribs->{class} =~ /^victor_(.*)$/) {
		$self->{existing_class} = $1;
		# will be printed in _text_block
		goto out;
	}
	my $output = "<" . $name;

	if (!cfg_isset('hide-ignored-attribs')) {
		foreach my $attr (@$attrseq) {
			# we don't use empty_element_tags, so let's
			# catch the "/" here
			if ($attr eq "/") {
				$empty_tag = 1;
				next;
			} 
			$output .= " $attr";
			if (defined($attribs->{$attr})) {
				$output .= "=\"" .
				encode_entities($attribs->{$attr}, '"') . '"';
			}
		}
	}
	if ($empty_tag && !$close_empty_tag) {
		$output .= " /";
	}
	print {$self->{out}} $output . ">";
out:
	if ($empty_tag) {
		# generate an artificial end event
		_end_element($self, $name, "", $close_empty_tag ? 0 : 1);
	}
	# we can wrap text into <span> elements from now on
	if ($name eq "body") {
		$self->{in_body} = 1;
	}
}

sub _end_element {
	my ($self, $name, $skipped_text, $silent) = @_;
	_anytag($self, $skipped_text);
	if (@{$self->{deleted_tags}} > 0 &&
		${$self->{deleted_tags}}[$#{$self->{deleted_tags}}] eq $name) {
		pop(@{$self->{deleted_tags}});
		return;
	}
	if (@{$self->{hidden_tags}} > 0 &&
		${$self->{hidden_tags}}[$#{$self->{hidden_tags}}] eq $name) {
		pop(@{$self->{hidden_tags}});
		return;
	}
	if ($name eq "pre") {
		$self->{in_pre}--;
		if ($self->{in_pre} < 0) {
			print STDERR "warning: <pre> closed but not opened\n";
			$self->{in_pre} = 0;
		}	
	}
	if (!$silent && _can_print($self)) {
		print {$self->{out}} "</" . $name . ">";
	}
}

sub _text_block {
	my ($self, $text) = @_;

	if (is_blank($text) || !$self->{in_body}) {
		print {$self->{out}} $text;
		return;
	}
	print {$self->{out}} "<span id=\"victor_an$self->{span_counter}\"";
	if ($self->{existing_class}) {
		print {$self->{out}} " class=\"victor_$self->{existing_class}\">$text";
		$self->{existing_class} = "";
	} else {
		print {$self->{out}} ">$text</span>";
	}
	$self->{span_counter}++;
}

# common code for opening/closing tags
sub _anytag {
	my ($self, $skipped_text) = @_;
	if (!_can_print($self)) {
		return;
	}
	if ($self->{insert_text}) {
		$skipped_text = $self->{insert_text} . $skipped_text;
		delete $self->{insert_text};
	}
	if ($self->{in_pre} > 0) {
		foreach my $line (split(/\n/, $skipped_text)) {
			_text_block($self, $line);
			print {$self->{out}} "<br />\n";
		}
	} else {
		_text_block($self, $skipped_text);
	}
}

sub _can_print {
	my $parser = shift;
	return @{$parser->{deleted_tags}} == 0;
}

1;
