# $Id: Expand.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Expand;

use strict;
use warnings;
use open ':locale';


use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&expand_curly);

# expands a{b,c{d,e},f}g{h,i} to qw(abgh abgi acdgh acdgi acegh acegi afgh afgi)
sub expand_curly {
	my $str = shift;
	# $str is broken up into "runs", which are either single-element
	# arrays (for text not enclosed in {...}) or arrays of words produced
	# by expansion of a pair of {...}
	# @runs contains arrayrefs to individual "runs"
	my @runs = ();
	my $depth = 0;
	# current run, not expanded
	my @current = ("");
	for (my $i = 0; $i < length($str); $i++) {
		my $c = substr($str, $i, 1);
		if ($c eq "{") {
			if ($depth == 0) {
				# end of single-element run
				if ($current[0] ne "") {
					push(@runs, [$current[0]]);
				}
				@current = ("");
			} else {
				$current[$#current] .= $c;
			}
			$depth++;
		} elsif ($c eq "}") {
			$depth--;
			if ($depth < 0) {
				die "unmatched `}'\n";
			}
			if ($depth == 0) {
				# end of toplevel {...}
				my @cur = map(expand_curly($_), @current);
				push(@runs, \@cur);
				@current = ("");
			} else {
				$current[$#current] .= $c;
			}
		} elsif ($c eq "," && $depth == 1) {
			# start new word (commas in deeper level will be
			# handled by a recursive call)
			push(@current, "");
		} else {
			$current[$#current] .= $c;
		}
	}
	if ($depth > 0) {
		die "unmatched `{'\n";
	}
	if ($current[0] ne "") {
		# last run
		push(@runs, [$current[0]]);
	}

	# now multiply the runs
	my @counters = map(0, @runs);
	my ($i, $i2);
	$i = $#counters;
	my @res;
	while ($i >= 0) {
		my $word = "";
		for ($i2 = 0; $i2 < @counters; $i2++) {
			$word .= $runs[$i2]->[$counters[$i2]];
		}
		push(@res, $word);
		$counters[$i]++;
		while ($i >= 0 && $counters[$i] == @{$runs[$i]}) {
			for ($i2 = $i; $i2 < @counters; $i2++) {
				$counters[$i2] = 0;
			}
			$i--;
			$counters[$i]++;
		}
		if ($i >= 0) {
			$i = $#counters;
		}
	}
	return @res;
}

1;
