# $Id: Containers.pm 356 2008-02-03 16:46:25Z michal $
# 
# store 'container.foo' features in the vector

package Victor::Features::Containers;

use strict;
use warnings;
use open ':locale';

use Victor::Cfg;

sub calculate_features {
	my ($block, $fv) = @_;
	my $counter = 0;
	foreach my $c (reverse(@{$block->{containers}})) {
		if (cfg_contains('class-block', $c)) {
			if ($counter < 1) { # XXX
				$counter++;
			}
			$fv->{"container.$c"} = $counter;
		} else {
		       $fv->{"container.$c"} = 1;
		}
		foreach my $cl (cfg_tag_classes($c)) {
			$fv->{"container.$cl"} = 1;
		}
	}
}

sub feature_supported {
	return $_[0] =~ /^container\./;
}

1;
