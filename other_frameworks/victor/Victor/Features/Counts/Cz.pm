# $Id: Cz.pm 389 2008-02-28 21:35:59Z michal $

package Victor::Features::Counts::Cz;

use strict;
use warnings;
use open ':locale';

use Victor::Cz::Tokenizer;
use Victor::Cz::Segmenter;
use Victor::Features::Util;
use POSIX;
use Encode;

sub calculate_features {
	my ($block, $fv) = @_;
	my $text;
	# sentence count
	my $num_s;
	my %tokens = (
		alpha => 0,
		num => 0,
		mixed => 0,
		other => 0);
	my $anyword_count = 0;
	# total length of words (alpha tokens)
	my $word_length;
	# number of word (alpha token) runs
	my $word_runs = 0;
	# line number in csts output
	my $lineno = 0;
	# index of first <s> tag
	my $first_s = -1;
	# index of first <f> tag after first <s> tag
	my $first_f = -1;
	# index of last word token
	my $last_alpha = 0;
	# index of last closing </s> tag
	my $last_s_end = -1;
	# last <d>. tag
	my $last_d = -1;

	$text = $block->{text};
	($text) = tokens($text);
	($text, $num_s) = segments($text);
	my @lines = split(/\n/, $text);
	foreach my $line (@lines) {
		my ($tag, $attr, $val) = $line =~ /<([^ >]+) *([^>]*)>(.*)/;
		if ($tag eq "s") {
			if ($first_s == -1) {
				$first_s = $lineno;
			}
		} elsif ($tag eq "f") {
			$anyword_count++;
			if ($first_s != -1 && $first_f == -1) {
				$first_f = $lineno;
			}
			if ($attr eq "cap" || $attr eq "") {
				$tokens{alpha}++;
				$word_length += length($val);
				if ($last_alpha != $lineno - 1) {
					$word_runs++;
				}
				$last_alpha = $lineno;
			} elsif ($attr eq "num") {
				$tokens{num}++;
			} elsif ($attr eq "mixed") {
				$tokens{mixed}++;
			} else {
				#print STDERR "<$tag \"$attr\"> => OTHER\n";
				$tokens{other}++;
			}
		} elsif ($tag eq "/s") {
			$last_s_end = $lineno;
		} elsif ($tag eq "d") {
			$last_d = $lineno;
		} else {
			#print STDERR "Unhandled: <$tag $attr>\n";
		}
		$lineno++;
	}
	$fv->{"sentence-not-finished"} =
		($last_s_end != -1 && $last_d != $last_s_end - 1) ? 1 : 0;
	$fv->{"sentence-not-started"} =
		($first_f != -1 && $lines[$first_f] !~ /^<f (cap|upper)/) ? 1 : 0;
	foreach my $what (keys(%tokens)) {
		$fv->{"token.$what-raw"} = $tokens{$what};
		$fv->{"token.$what-abs"} = find_closest($tokens{$what},
			"token.$what-abs");
		$fv->{"token.$what-rel"} = scale($tokens{$what}, $anyword_count,
			"token.$what-rel");
	}
	$fv->{"sentence.count-raw"} = $num_s;
	$fv->{"sentence.count"} = find_closest($num_s, "sentence.count");
	$fv->{"avg-word-length"} = $tokens{alpha}
		? find_closest(floor($word_length / $tokens{alpha} + 0.5),
			'avg-word-length')
		: 0;
	$fv->{"avg-word-run"} = $word_runs
		? find_closest(floor($anyword_count / $word_runs + 0.5),
			'avg-word-run')
		: 0;
}

sub feature_supported {
	return $_[0] =~ /^(sentence.count|token.(alpha|num|mixed|other)-(abs|rel)|sentence-not-(started|finished)|avg-word-(length|run))$/;
}

1;
