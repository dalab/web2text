#!/usr/bin/perl -w
#
# $Id: split.pl 357 2008-02-03 16:48:06Z michal $

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::SplitHTML;
use Victor::Features;

GetOptions("_fileargs" => "1,1");

my @blocks;

split_html($ARGV[0], \@blocks);

calculate_fv(@blocks);
foreach my $block (@blocks) {
	print "--- BLOCK " . $block->{id} . " ---\n";
	print $block->{text} . "\n";
	foreach my $k (sort(keys(%{$block->{fv}}))) {
		print "\t$k $block->{fv}->{$k}\n";
	}
	print "\tdistance (" . join(",", @{$block->{distance}}) . ")\n";
	print "\tcontainers (" . join(",", @{$block->{containers}}) . ")\n";
	print "\ttd_group (" . join(",", @{$block->{td_group}}) . ")\n";
}

