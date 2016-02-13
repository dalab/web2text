# $Id: Util.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Features::Util;

use strict;
use warnings;
use open ':locale';

use Victor::Cfg;

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&find_closest &scale);

sub find_closest {
	my ($actual, $name) = @_;
	my @values = cfg_get_array("feature-$name-values");
	foreach my $val (sort {$b <=> $a} @values) {
		if ($actual >= $val) {
			return $val;
		}
	}
	return 0;
}

sub scale {
	my ($actual, $total, $name) = @_;
	return 0 if $total == 0;
	my $scale = cfg_get_int("feature-$name-scale");
	$actual = sprintf("\%d", $actual * ($scale + 1) / $total);
	if ($actual > $scale) {
		$actual = $scale;
	}
	return $actual;
}

1;
