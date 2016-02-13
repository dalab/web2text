#!/usr/bin/perl -w
#
# $Id: check-deps.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

check-deps.pl - check if Victor dependencies are installed

=head1 SYNOPSIS

check-deps.pl [-v|--verbose]

=cut

use strict;
use warnings;
use open ':locale';

my @missing_modules = ();
my $missing_curses = 0;
my $missing_tidy = 0;
my $missing_crf = 0;
my $opt_verbose = 0;

# do the option processing manualy in case Getopt::Long or Pod::Usage
# is missing (which is quite unlikely)
while (@ARGV) {
	my $arg = shift @ARGV;
	if ($arg eq "-v" || $arg eq "--verbose") {
		$opt_verbose = 1;
	}
	if ($arg =~ /^--(help|usage)$/) {
		print "Usage:\n";
		print "    check-deps.pl [-v|--verbose]\n";
		exit 0;
	}
}

check_module(qw(
	Cwd Encode File::Basename File::Copy File::Spec File::Temp
	Getopt::Long HTML::Entities HTML::Parser List::Util POSIX Pod::Usage
));
if (!check_module("Curses")) {
	$missing_curses = 1;
}
if (!check_binary("tidy")) {
	$missing_tidy = 1;
}
if (!check_binary(qw(crf_learn crf_test))) {
	$missing_crf = 1;
}
if (!@missing_modules && !$missing_tidy && !$missing_crf) {
	print "All dependencies OK\n";
	exit 0;
}
if (@missing_modules) {
	print "Please install the following Perl modules: " .
		join(", ", @missing_modules) . "\n";
}
if ($missing_curses) {
	print "Note that the Curses module is only required by the annotate.pl script\n";
}
if ($missing_tidy) {
	print "Please install HTML tidy\n";
}
if ($missing_crf) {
	print "Please install CRF++ (available from http://crfpp.sourceforge.net/)\n";
}
exit 1;

sub check_module {
	my $ok = 1;
	foreach my $mod (@_) {
		print "Checking for $mod... ";
		if (eval("require $mod")) {
			if ($opt_verbose) {
				my $modfile = "$mod.pm";
				$modfile =~ s/::/\//g;
				print "ok ($INC{$modfile})\n";
			} else {
				print "ok\n";
			}
		} else {
			push(@missing_modules, $mod);
			$ok = 0;
			if ($opt_verbose) {
				print "not found ($@)\n";
			} else {
				print "not found\n";
			}
		}
	}
	return $ok;
}

my @PATH;
sub check_binary {
	if (!@PATH) {
		@PATH = split(/:/, $ENV{'PATH'});
		if (!@PATH) {
			@PATH = (".");
		}
	}
	my $ok = 1;
BIN:	foreach my $bin (@_) {
		print "Checking for $bin... ";
		foreach my $dir (@PATH) {
			next if $dir eq "";
			if (-x "$dir/$bin") {
				if ($opt_verbose) {
					print "ok ($dir/$bin)\n";
				} else {
					print "ok\n";
				}
				next BIN;
			}
		}
		$ok = 0;
		print "not found\n";
	}
	return $ok;
}

