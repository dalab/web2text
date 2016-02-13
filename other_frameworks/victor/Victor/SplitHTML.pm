# $Id: SplitHTML.pm 356 2008-02-03 16:46:25Z michal $

=head1 NAME

Victor::SplitHTML

=head1 SYNOPSIS

C<split_html($file, \@result);>

=head1 DESCRIPTION

Takes a precleaned file as input and stores it as an array of blocks.
Each block is a hash with these entries:

=over

=item "id"

id of the block (matches corresponding C<E<lt>span id="victor_an$idE<gt>>)

=item "text"

plaintext content of the block

=item "containers" 

array of parent elements, from outermost to innermost

=item "distance"

array of tags seen since last block, "x" stands for opening tag <x>
and "/x" stands for closing tag </x>

=item "td_group", "div_group"

array of ids of blocks that are in the same td_group or div_group

=back

=cut

package Victor::SplitHTML;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&split_html);

use Victor::Temp;

use HTML::Parser;

sub split_html {
	my ($file, $blocks) = @_;
	my $ref;
	# if called with a reference to scalar, store the new filename
	# in the scalar
	if (ref($file) eq "SCALAR") {
		$ref = $file;
		$file = $$file;
	} else {
		$ref = \$file;
	}
	_split_html($file, $blocks);
	if (!@$blocks) {
		# assume that input is not precleaned, retry
		use Victor::Tidy;
		use Victor::PrepareHTML;
		my ($out, $err) = tidy_html($file, "tidy.cfg");
		if (!$out) {
			die $err;
		}
		my ($fh, $tmpfile) = tempfile();
		prepare_html($out, $fh);
		close($fh);
		_split_html($tmpfile, $blocks);
		$$ref = $tmpfile;
	}
}

sub _split_html {
	my ($in, $result) = @_;
	my $p = HTML::Parser->new(api_version => 3,
		start_h => [\&_start_element, "self,tagname,attr"],
		end_h => [\&_end_element, "self,tagname"],
	);
	# generate end events for <tags />
	$p->empty_element_tags(1);
	$p->utf8_mode(0);
	$p->{containers} = [];  # current path in element tree
	$p->{distance} = [];    # tags encountered since last split
	$p->{td_groups} = [[]];
	$p->{div_groups} = [[]];
	$p->{result} = $result;
	open(my $fh, "<", $in) or die;
	$p->parse_file($fh) or die "$in: $!\n";
	close($fh);
}

sub _start_element {
	my ($self, $name, $attr) = @_;
	if ($name eq "span" && defined($attr->{id}) &&
		$attr->{id} =~ /^victor_an([0-9]+)/) {
		$self->{block_id} = $1;
		if (defined($attr->{class}) &&
			$attr->{class} =~ /^victor_(.*)/) {
			$self->{class} = $1;
		} else {
			$self->{class} = undef;
		}
		$self->handler(text => [], "\@{dtext}");
		$self->handler(end  => \&_end_span, "self");
		push(@{$self->{td_groups}->[0]}, $self->{block_id});
		push(@{$self->{div_groups}->[0]}, $self->{block_id});
	} else {
		if ($name eq "td") {
			unshift(@{$self->{td_groups}}, []);
		}
		if ($name eq "div") {
			unshift(@{$self->{div_groups}}, []);
		}
		push(@{$self->{containers}}, $name);
		push(@{$self->{distance}}, $name);
	}
}

sub _end_element {
	my ($self, $name) = @_;
	if ($name eq "td") {
		shift(@{$self->{td_groups}});
	}
	if ($name eq "div") {
		shift(@{$self->{div_groups}});
	}
	pop(@{$self->{containers}});
	push(@{$self->{distance}}, "/" . $name);
}

sub _end_span {
	my $self = shift;
	my $text = join("", @{$self->handler("text")});
	my $block = {
		"id" => $self->{block_id},
		"text" => $text,
		"containers" => [],
		"distance" => [],
		"td_group" => $self->{td_groups}->[0] || [],
		"div_group" => $self->{div_groups}->[0] || [],
	};
	if (defined($self->{class})) {
		$block->{class} = $self->{class};
	}
	@{$block->{containers}} = @{$self->{containers}};
	@{$block->{distance}} = @{$self->{distance}};
	$self->{result}->[$block->{id}] = $block;
	$self->handler("text", undef);
	$self->handler("end", \&_end_element, "self,tagname");
	$self->{distance} = [];
}

1;
