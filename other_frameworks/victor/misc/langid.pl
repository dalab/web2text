#!/usr/bin/perl

# $Id$

use Data::Dumper;

use Victor::Features::CzLangID;
use Victor::Getopt;

GetOptions();

while (<>) {
	my %fv;
	Victor::Features::CzLangID::calculate_features({text => $_}, \%fv);
	print Dumper(\%fv);
}
