# $Id: Lists.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Features::CLEANEVAL::Lists;

use strict;
use warnings;
use open ':locale';

my @numbers = (
	'[0-9]+',
	'[a-z]+',
	'[A-Z]+',
);
my $numbers_offset = 100;

my @delims = (
	'\.',
	'\)',
	'\/',
	' -',
);

# build a regexp in the form '^\s*(num1|num2|...)(delim1|delim2|...)'
my $regexp = '^\s*(' . join('|', @numbers) . ')(' . join('|', @delims) . ')';


sub calculate_features {
	my ($block, $fv) = @_;
	if ($block->{"text"} !~ /$regexp/) {
		$fv->{"looks-like-bullet"} = 0;
		return;
	}
	my $num = $1;
	my $del = $2;
	my $res = 0;
	my $i;
	# This sucks. The =~ operator above only tells us whether the whole
	# pattern matched, but not how it matched :-(
	$i = 1;
	foreach my $pat (@numbers) {
		if ($num =~ /$pat/) {
			$res = $i * $numbers_offset;
			last;
		}
		$i++;
	}
	$i = 1;
	foreach my $pat (@delims) {
		if ($del =~ /$pat/) {
			$res += $i;
			last;
		}
		$i++;
	}
	if ($res  < 101) {
		die "internal error, pattern matched but we don't know how";
	}
	$fv->{"looks-like-bullet"} = $res;
}

sub feature_supported {
	return $_[0] eq "looks-like-bullet";
}

1;
