#!/usr/bin/perl -w
# $Id: check-classes.pl 356 2008-02-03 16:46:25Z michal $
# 
# read elements.txt and print tag classes


use strict;
use warnings;
use open ':locale';

use Victor::Cfg;


foreach my $tag (cfg_all_tags()) {
	next if cfg_contains('tag-delete', $tag);
	next if cfg_contains('tag-ignore', $tag);
	my @classes = cfg_tag_classes($tag);
	if (@classes > 0) {
		print "$tag: " . join(" ", @classes) . "\n";
	} else {
		print "$tag: NO CLASSES\n";
	}	
}
