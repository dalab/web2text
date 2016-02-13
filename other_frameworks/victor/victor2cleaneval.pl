#!/usr/bin/perl -w
#
# $Id: victor2cleaneval.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

victor2cleaneval.pl - convert annotated html document to cleaneval format

=head1 SYNOPSIS

victor2cleaneval.pl infile [outfile]

=head1 SEE ALSO

http://cleaneval.sigwac.org.uk/annotation_guidelines.html

=cut

use strict;
use warnings;
use open ':locale';

use Victor::SplitHTML;
use Victor::Cfg;
use Victor::Getopt;
use Victor::Output::Cleaneval;

load_cfg("cleaneval.conf");
GetOptions("_fileargs" => "1,2");

my $in = $ARGV[0];
my @blocks;

split_html($in, \@blocks);
my $res = output_cleaneval({output => $ARGV[1]}, \@blocks);

if (!$res) {
	print STDERR "$in: warning: source file not annotated\n";
}
exit 0;

