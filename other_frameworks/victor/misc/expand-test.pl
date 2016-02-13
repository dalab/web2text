#!/usr/bin/perl -w

use strict;
use warnings;
use open ':locale';

use Victor::Expand;
use Data::Dumper;

my ($shell, $me);
while (<>) {
	chomp;
	$me = join(" ",expand_curly($_));
	$shell = `echo $_`;
	chomp($shell);
	if ($me eq $shell) {
		print "OK \"$me\"\n";
	} else {
		print "BUG me: \"$me\" vs. shell: \"$shell\"\n";
	}
}
