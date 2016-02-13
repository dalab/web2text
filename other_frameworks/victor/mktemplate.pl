#!/usr/bin/perl
#
# $Id: mktemplate.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

mktemplate.pl - create a CRF++ template from a template ;-)

=head1 SYNOPSIS

mktemplate.pl [infile [outfile]]

=head1 DESCRIPTION

Transforms B<< %x[<num>,<feature name>] >> to B<< %x[<num>,<feature num>] >>.
Also supports {<feature1 name>,<feature2 name>} syntax, expanding to multiple
B<%x> macros delimited by "/".


=head1 SEE ALSO

http://chasen.org/~taku/software/CRF++/

=cut

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::MkTemplate;

GetOptions("_fileargs" => "0,2");

mktemplate($ARGV[0], $ARGV[1]);
