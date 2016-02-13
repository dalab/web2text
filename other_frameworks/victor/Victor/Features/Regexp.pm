# $Id: Regexp.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Features::Regexp;

use strict;
use warnings;
use open ':locale';

use Victor::Cfg;

my $loaded = 0;
my %patterns;

sub calculate_features {
	my ($block, $fv) = @_;
	if (!$loaded) {
		foreach my $var (cfg_list_variables("^feature-regexp-")) {
			my $name = $var;
			$name =~ s/^feature-regexp-//;
			$patterns{$name} = [cfg_get_array($var)];
		}
	}
	foreach my $name (keys(%patterns)) {
		my $i = 1;
		$fv->{"regexp.$name"} = 0;
		foreach my $re (@{$patterns{$name}}) {
			if ($block->{text} =~ /$re/i) {
				$fv->{"regexp.$name"} = $i;
				last;
			}
			$i++;
		}
	}
}

sub feature_supported {
	return $_[0] =~ /^regexp\./;
}

1;
