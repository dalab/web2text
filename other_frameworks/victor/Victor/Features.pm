# $Id: Features.pm 393 2008-02-29 21:18:07Z michal $
#
# naive feature vector implementation

package Victor::Features;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&calculate_fv &feature_supported);

use Victor::Cfg;
use Victor::Features::Util;
use Victor::Features::Counts::Generic;
use Victor::Features::Counts::Cz;
use Victor::Features::Characters;
use Victor::Features::Containers;
use Victor::Features::Splits;
use Victor::Features::CLEANEVAL;
use Victor::Features::Regexp;
use Victor::Features::CzLangID;

sub calculate_fv {
	my $numblocks = find_closest(scalar(@_), 'document-block-count');
	my $numwords_raw = 0;
	my $numsentences_raw = 0;
	foreach my $block (@_) {
		my %fv;
		foreach my $module (cfg_get_array('feature-modules')) {
			no strict 'refs';
			&{"Victor::Features::${module}::calculate_features"}($block, \%fv);
		}
		$block->{fv} = \%fv;
		#if (!$block->{fv}->{"container.a"}) {
			$numwords_raw += $fv{"token.alpha-raw"};
			$numsentences_raw += $fv{"sentence.count-raw"};
		#}
	}
	my $numwords = find_closest($numwords_raw, 'document-word-count');
	my $numsentences = find_closest($numsentences_raw,
		'document-sentence-count');

	### special features
	my %twins;
	my %max_group = ("td" => 0, "div" => 0);
	for (my $i = 0; $i < scalar(@_); $i++) {
		my $block = $_[$i];

		foreach my $tag ("td", "div") {
			if (!exists($block->{fv}->{"$tag-group.word-count"})) {
				my $count = 0;
				foreach my $id (@{$block->{"${tag}_group"}}) {
					if (!$_[$id]->{fv}->{"container.a"}) {
						$count += $_[$id]->{fv}->
							{"token.alpha-raw"};
					}
				}
				$count = scale($count, $numwords_raw, 
						"$tag-group-word");

				foreach my $id (@{$block->{"${tag}_group"}}) {
					$_[$id]->{fv}->{"$tag-group.word-rel"}
							= $count;
				}
				# in case the array is empty
				$block->{fv}->{"$tag-group.word-rel"} =
						$count;
				if ($count > $max_group{$tag}) {
					$max_group{$tag} = $count;
				}
			}
		}

		my $text = $block->{text};
		# normalize text to sequences of alnum separated by '-'
		$text =~ s/^\W*//;
		$text =~ s/\W*$//;
		$text =~ s/\W+/-/g;
		if (exists($twins{$text})) {
			push(@{$twins{$text}}, $block);
		} else {
			$twins{$text} = [$block];
		}
	}
	foreach my $arr (values(%twins)) {
		my $num = find_closest(scalar(@$arr), 'twins');
		foreach my $block (@$arr) {
			$block->{fv}->{"twins"} = $num;
		}
		$arr->[0]->{fv}->{"first-twin"} = 1;
	}
	for (my $i = 0; $i < scalar(@_); $i++) {
		my $block = $_[$i];

		$block->{fv}->{"position"} = scale($i, scalar(@_), 'position');
		$block->{fv}->{"document.block-count"} = $numblocks;
		$block->{fv}->{"document.word-count"} = $numwords;
		$block->{fv}->{"document.sentence-count"} = $numsentences;
		$block->{fv}->{"document.max-td-group"} = $max_group{"td"};
		$block->{fv}->{"document.max-div-group"} = $max_group{"div"};
	}
}

# check if feature is known
sub feature_supported {
	my $verbose = 0;
	if (@_ > 0 && $_[0] eq "_verbose") {
		$verbose = 1;
		shift;
	}
ARG:	foreach my $feature (@_) {
		if ($feature =~ /^(document|(td|div)-group)\.|^(twins|first-twin|position)$/) {
			next ARG;
		}
		foreach my $module (cfg_get_array('feature-modules')) {
			no strict 'refs';
			if (&{"Victor::Features::${module}::feature_supported"}($feature)) {
				next ARG;
			}
		}
		if ($verbose) {
			print STDERR "feature \"$feature\" not supported by any module\n";
		}
		return 0;
	}
	return 1;
}

1;
