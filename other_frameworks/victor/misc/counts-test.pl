#!/usr/bin/perl -w
#
# $Id: counts-test.pl 382 2008-02-26 20:42:30Z michal $

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::Features::Counts::Generic;
use Victor::Features::Counts::Cz;

my $only_difs = 0;
GetOptions("only-diffs" => \$only_difs);

while (<>) {
	my %generic;
	my %cz;
	my $text = $_;
	my $printed = 0;
	Victor::Features::Counts::Generic::calculate_features({text => $text},
		\%generic);
	Victor::Features::Counts::Cz::calculate_features({text => $text}, \%cz);
	if (!$only_difs) {
		print "text: $text";
		$printed = 1;
	}
	foreach my $k (sort(keys %{{%generic, %cz}})) {
		my $g = exists($generic{$k}) ? $generic{$k} : "(none)";
		my $c = exists($cz{$k}) ? $cz{$k} : "(none)";
		if (!$only_difs || $g ne $c) {
			if (!$printed) {
				print "text: $text";
				$printed = 1;
			}
			print "\t$g\t$c\t($k)\n";
		}
	}
}
