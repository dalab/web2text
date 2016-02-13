#!/usr/bin/perl -w
#
# $Id: victor2cleaneval.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

convert.pl - convert annotated html document to selected format

=head1 SYNOPSIS

convert.pl --format format infile [outfile]

=head1 OPTIONS

=over

=item B<-f|--format format>

Output format, one of HTML, CRF or Cleaneval.

=back


=cut

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::Cfg;
use Victor::SplitHTML;
use Victor::Output::Cleaneval;
use Victor::Output::CRF;
use Victor::Output::HTML;

my $opt_format;
GetOptions(
	"_fileargs" => "1,2",
	"f|format=s" => \$opt_format);
$opt_format = lc($opt_format);
my $out_handler;
if ($opt_format eq "victor") {
	$out_handler = \&output_html;
} elsif ($opt_format eq "crf") {
	$out_handler = \&output_crf;
} elsif ($opt_format eq "cleaneval") {
	$out_handler = \&output_cleaneval;
} else {
	print STDERR "unknown output format: $opt_format\n";
	print STDERR "available formats are: victor, crf, cleaneval\n";
	exit 1;
}

my $in = $ARGV[0];
my @blocks;

split_html($in, \@blocks);
&$out_handler({
		output => $ARGV[1],
		train => exists($blocks[0]->{class}) ? 1 : 0
	}, \@blocks);
exit 0;

