#!/usr/bin/perl -w

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::CLEANEVAL::Scorer;

my $directory = ".";
my $maxsize = 0;
GetOptions("_fileargs" => "2,2",
           "d|directory=s" => \$directory,
           "m|maxsize=i" => \$maxsize);

my %score = scorer(clean => $ARGV[0], "ref" => $ARGV[1],
	directory => $directory, maxsize => $maxsize * 1024);

foreach my $type (keys(%score)) {
	print "$type: $score{$type}\n";
}
