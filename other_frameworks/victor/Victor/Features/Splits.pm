# $Id: Splits.pm 356 2008-02-03 16:46:25Z michal $
#
# store 'split.foo' features in the vector, eg in
# 
#   aaa <b> bbb </b> ccc
# 
# bbb would have "split.b" => 1 and ccc would have "split./b" => 1


package Victor::Features::Splits;

use strict;
use warnings;
use open ':locale';

use Victor::Cfg;
use Victor::Features::Util;

sub calculate_features {
	my ($block, $fv) = @_;
	my %res;
	foreach my $tag (@{$block->{distance}}) {
		if (substr($tag, 0, 1) eq "/") {
			$tag = substr($tag, 1);
		}
		if (!$res{$tag}) {
			$res{$tag} = 1;
		} else {
			$res{$tag}++;
		}
		foreach my $cl (cfg_tag_classes($tag)) {
			if (!$res{$cl}) {
				$res{$cl} = 1;
			} else {
				$res{$cl}++;
			}
		}
	}
	foreach my $k (keys(%res)) {
		$fv->{"split.$k"} = find_closest($res{$k}, 'split');
	}
}

sub feature_supported {
	return $_[0] =~ /^split\./;
}

1;
