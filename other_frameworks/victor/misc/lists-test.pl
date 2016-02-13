#!/usr/bin/perl

use strict;
use warnings;
use open ':locale';

use Victor::Features::CLEANEVAL;

my %block;
my  %fv;

while (<>) {
	chomp;
	$block{text} = $_;
	Victor::Features::CLEANEVAL::Lists::calculate_features(\%block, \%fv);
	print "$fv{'looks-like-bullet'}\n";
}

