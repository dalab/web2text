#!/usr/bin/perl

use strict;
use warnings;
use open ':locale';

use Victor::SplitHTML;
use Victor::Output::HTML;
use Victor::Getopt;

GetOptions("_fileargs" => "2,2");
my $anno = $ARGV[0];
my $new = $ARGV[1];

my @blocks_a;
my @blocks_n;

split_html($anno, \@blocks_a);
split_html(\$new, \@blocks_n);


my $i_a = 0;
my $i_n = 0;
for ($i_a = 0; $i_a < @blocks_a; $i_a++) {
	my $b_a = $blocks_a[$i_a];
	my $b_n = $blocks_n[$i_n];
	while ($i_n < @blocks_n && $b_a->{text} ne $b_n->{text}) {
		#print STDERR "---\nskipping:\n$b_a->{text}\n$b_n->{text}\n";
		
		$b_n = $blocks_n[++$i_n];
	}
	if ($i_n >= @blocks_n) {
		print STDERR "$anno: error: couldn't match block #$i_a ($b_a->{text})\n";
		exit 1;
	}
	#print STDERR "found: \"$b_a->{text}\" ($b_a->{class})\n";
	$b_n->{class} = $b_a->{class};
}

output_html({orig => $new, output => \*STDOUT}, \@blocks_n);
