#!/usr/bin/perl -w
#
# $Id: anno-stat.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

anno-stat.pl - print annotation statistics for HTML files

=head1 SYNOPSIS

anno-stat.pl file ...

=head1 DESCRIPTION

This script prints out number and percentage of annotated blocks in a given
file. The output is

  filename: <annotated>/<total> (<percentage>)

=cut


use strict;
use warnings;
use open ':locale';

use Victor::SplitHTML;
use Victor::Getopt;

GetOptions("_fileargs" => "1,");

foreach my $file (@ARGV) {
	my @blocks;
	my $annotated = 0;
	split_html($file, \@blocks);
	foreach my $block (@blocks) {
		if ($block->{class}) {
			$annotated++;
		}
	}
	printf("%s: %d/%d (%.2f%%)\n", $file, $annotated, scalar(@blocks),
		$annotated * 100 / scalar(@blocks));
}

