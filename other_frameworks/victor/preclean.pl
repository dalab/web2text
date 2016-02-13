#!/usr/bin/perl -w
#
# $Id: preclean.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

preclean.pl - preclean html document for annotation or cleaning

=head1 SYNOPSIS

preclean.pl infile [outfile]

=cut

use strict;
use warnings;
use open ':locale';


use Victor::Getopt;
use Victor::Tidy;
use Victor::PrepareHTML;

GetOptions("_fileargs" => "1,2");

my $file = $ARGV[0];
my $out;
my $err;
($out, $err) = tidy_html($file, "tidy.cfg");
if (!$out) {
	die $err;
}

prepare_html($out, $ARGV[1]);

