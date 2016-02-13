#!/usr/bin/perl
#
# $Id: dump-crf.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

dump-crf.pl - dump the block sequence in a format that can be fed to CRF++

=head1 SYNOPSIS

dump-crf.pl [--train] [--output outfile] file ...

=head1 OPTIONS

=over

=item B<--train>

assume the blocks are already annotated and generate training data

=item B<-o|--output file>

output file (default is stdout)

=back

=head1 SEE ALSO

  http://chasen.org/~taku/software/CRF++/

=cut

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::Output::CRF;

my $opt_train;
my $opt_output;
GetOptions(
	"_fileargs" => "1,",
	"train" => \$opt_train,
	"o|output=s" => \$opt_output,
);

if ($opt_train) {
	$opt_train = 2;
}

output_crf({train => $opt_train, output => $opt_output}, @ARGV);
