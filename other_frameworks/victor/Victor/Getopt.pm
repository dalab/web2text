# $Id: Getopt.pm 356 2008-02-03 16:46:25Z michal $

=head1 NAME

Victor::Getopt - Getopt::Long::GetOptions wrapper

=head1 SYNOPSIS

GetOptions(["_fileargs" => "min[,max]",] "arg" => value, ...)

=head1 DESCRIPTION

This is a Getopt::Long::GetOptions wrapper that adds B<--config>, B<--usage>,
B<--help> and B<--man> options and calls exit if the command line is incorrect.
The optional I<"_fileargs"> argument specifies the minimum and maximum number
of file arguments. I<max> defaults to unlimited if not specified.

=head1 SEE ALSO

Getopt::Long

=cut

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT $opt_verbose $opt_config);
@ISA = qw(Exporter);
@EXPORT = qw(&GetOptions &verbose $opt_verbose);

use Victor::Cfg;

use Getopt::Long ();
use Pod::Usage;

Getopt::Long::Configure("gnu_getopt");

$opt_verbose = 0;
$opt_config = "";

sub GetOptions {
	my $files_min = 0;
	my $files_max = -1;
	if (@_ > 0 && $_[0] eq "_fileargs") {{
		shift;
		my @_fileargs = split(/,/, shift);
		if (@_fileargs < 1 || @_fileargs > 2 ||
			($_fileargs[0] !~ /^\d+$/) ||
			(@_fileargs == 2 && $_fileargs[1] !~ /^-?\d+$/)) {
			warn("warning: _fileargs argument must be followed by a string with one or two numbers separated by commas");
			last;
		}
		$files_min = $_fileargs[0];
		if (@_fileargs == 2) {
			$files_max = $_fileargs[1];
		}
	}}
	my $res = Getopt::Long::GetOptions(@_,
		"config=s" => \$opt_config,
		"usage"    => sub { pod2usage(-exitval => 0, -verbose => 0); },
		"help"     => sub { pod2usage(-exitval => 0, -verbose => 1); },
		"man"      => sub { pod2usage(-exitval => 0, -verbose => 2); },
		"v|verbose" => sub { $opt_verbose++; },
		"q|quiet"   => sub { $opt_verbose--; },
	);
	if (@ARGV < $files_min) {
		print STDERR "Missing argument\n";
		$res = 0;
	}
	if ($files_max >= 0 && @ARGV > $files_max) {
		print STDERR "Too many arguments\n";
		$res = 0;
	}
	if (!$res) {
		pod2usage(-exitval => 1, -verbose => 0, -output => ">&2");
		exit 1;
	}
	if ($opt_config) {
		load_cfg($opt_config);
	}
}

sub verbose {
	my $level = shift;
	if (ref($level) eq "HASH") {
		foreach my $l (sort {$b <=> $a} keys(%$level)) {
			if ($l <= $opt_verbose) {
				print STDERR $level->{$l};
				return;
			}
		}
	} elsif ($level <= $opt_verbose) {
		print STDERR @_;
	}
}

1;
