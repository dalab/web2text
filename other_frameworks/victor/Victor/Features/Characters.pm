# $Id: Characters.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Features::Characters;

use strict;
use warnings;
use open ':locale';

use Victor::Features::Util;

sub calculate_features {
	my ($block, $fv) = @_;
	my %counts = (
		alpha => 0,
		num => 0,
		punct => 0,
		white => 0,
		other => 0);
	for (my $i = 0; $i < length($block->{text}); $i++) {
		my $char = substr($block->{text}, $i, 1);
		if ($char =~ /\p{IsAlpha}/) {
			$counts{alpha}++;
		} elsif ($char =~ /[0-9]/) {
			$counts{num}++;
		} elsif ($char =~ /[,:;\.\?!]/) {
			$counts{punct}++;
		} elsif ($char =~ /\s/) {
			$counts{white}++;
		} else {
			$counts{other}++;
		}
	}
	foreach my $what (keys(%counts)) {
		$fv->{"char.$what-rel"} = scale($counts{$what},
				length($block->{text}), "char.$what-rel");
	}
}

sub feature_supported {
	return $_[0] =~ /^char.(alpha|num|punct|white|other)-rel$/;
}

1;
