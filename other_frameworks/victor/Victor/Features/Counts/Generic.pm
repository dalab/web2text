# $Id: Generic.pm 366 2008-02-21 21:17:37Z michal $

package Victor::Features::Counts::Generic;

use strict;
use warnings;
use open ':locale';

use Victor::Features::Util;
use POSIX;

sub calculate_features {
	my ($block, $fv) = @_;
	my @tokens = grep($_ ne "", split(/\b|\s+/, $block->{"text"}));
	my %tokens = (
		alpha => 0,
		num => 0,
		mixed => 0,
		other => 0);
	my $sentences = 0;
	my $first_word = -1;
	my $last_word = -1;
	my $last_period = -1;
	my $word_length = 0;
	my $word_runs = 0;
	my $anyword_count = scalar(@tokens);
	for (my $i = 0; $i < scalar(@tokens); $i++) {
		if ($tokens[$i] =~ /[\.\?!]$/) {
			$sentences++;
			$last_period = $i;
		}
		if ($tokens[$i] =~ /^\p{IsAlpha}*$/) {
			if ($word_runs == 0 || $last_word != $i - 1) {
				$word_runs++;
			}
			if ($first_word == -1) {
				$first_word = $i;
			}
			$last_word = $i;
			$tokens{alpha}++;
			$word_length += length($tokens[$i]);
		} elsif ($tokens[$i] =~ /^[0-9]*$/) {
			$tokens{num}++;
		} elsif ($tokens[$i] =~ /^\p{IsAlnum}*$/) {
			$tokens{mixed}++;
		} elsif ($tokens[$i] =~ /^[,:;\.\?!]*$/) {
			$anyword_count--; # punctiation doesn't count as tokens
		} else {
			$tokens{other}++;
		}
	}
	foreach my $what (keys(%tokens)) {
		$fv->{"token.$what-raw"} = $tokens{$what};
		$fv->{"token.$what-abs"} = find_closest($tokens{$what},
			"token.$what-abs");
		$fv->{"token.$what-rel"} = scale($tokens{$what}, $anyword_count,
			"token.$what-rel");
	}
	$fv->{"sentence.count-raw"} = $sentences;
	$fv->{"sentence.count"} = find_closest($sentences, "sentence.count");
	$fv->{"avg-word-length"} = $tokens{alpha}
		? find_closest(floor($word_length / $tokens{alpha} + 0.5),
			'avg-word-length')
		: 0;
	$fv->{"avg-word-run"} = $word_runs
		? find_closest(floor($anyword_count / $word_runs + 0.5),
			'avg-word-run')
		: 0;
	if ($first_word == -1) {
		$fv->{"sentence-not-started"} = 1;
		$fv->{"sentence-not-finished"} = 1;
	} else {
		if ($tokens[$first_word] =~ /^\p{IsUpper}/) {
			$fv->{"sentence-not-started"} = 0;
		} else {
			$fv->{"sentence-not-started"} = 1;
		}
		if ($last_period < $last_word) {
			$fv->{"sentence-not-finished"} = 1;
		} else {
			$fv->{"sentence-not-finished"} = 0;
		}
	}
}

sub feature_supported {
	return $_[0] =~ /^(sentence.count|token.(alpha|num|mixed|other)-(abs|rel)|sentence-not-(started|finished)|avg-word-(length|run))$/;
}

1;
