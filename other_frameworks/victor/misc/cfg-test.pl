#!/usr/bin/perl

use strict;
use warnings;
use open ':locale';

use Victor::Cfg;
use Victor::Getopt;
use Data::Dumper;

GetOptions("_fileargs" => "0,0");


$Data::Dumper::Terse = 1;
print "crf-features (" . scalar(cfg_get_array('crf-features')) . ") = " .
	Dumper([cfg_get_array('crf-features')]) . "\n";
print "class-bold = " . Dumper({cfg_get_set('class-bold')}) . "\n";
print "hide-ignored-tags = " . Dumper(cfg_isset('hide-ignored-tags')) . "\n";
print "h2 = " . Dumper([cfg_tag_classes('h2')]) . "\n";
print "classes = " . Dumper([cfg_list_variables("^class-")]) . "\n";
