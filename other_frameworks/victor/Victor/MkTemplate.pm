# $Id: MkTemplate.pm 368 2008-02-22 10:19:38Z michal $
=head1 NAME

Victor::MkTemplate

=head1 SYNOPSIS

mktemplate([$in [, $out [,$infilename]]])

=head1 DESCRIPTION

Transforms B<< %x[<num>,<feature name>] >> to B<< %x[<num>,<feature num>] >>.
Also supports {<feature1 name>,<feature2 name>} syntax, expanding to multiple
B<%x> macros delimited by "/".

=over

=cut

package Victor::MkTemplate;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&mktemplate);

use Victor::Cfg;
use Victor::Expand;
use Victor::Features;

sub f2num {
	my ($f, $fname, $hash) = @_;
	if (defined($hash->{$f})) {
		return $hash->{$f};
	}
	die "$fname$.: error: feature \"$f\" not listed in crf-features:\n";
	return $f;
}

sub features2num {
	my ($name, $row, $col, $fname, $hash) = @_;
	my @features;
	my @rows;
	eval {
		@features = expand_curly($col);
		@rows = expand_curly($row);
		1;
	} or die "$ARGV:$.: error: $@\n";
	my @res = ();
	foreach my $r (@rows) {
		my @line = ();
		foreach my $f (@features) {
			push(@line, "\%x[$r," . f2num($f, $fname, $hash) . "]");
		}
		if (scalar(@rows) > 1) {
			push(@res, "${name}_$r:" . join("/",  @line));
		} else {
			push(@res, "$name:" . join("/",  @line));
		}
	}
	return join("\n", @res);
}

=item mktemplate([$in [, $out [, $infilename]]])

=over

=item B<$in> - input file name or handle, defaults to STDIN

=item B<$out> - output file name or handle, defaults to STDOUT

=item B<$infilename> - name of inputfile for error reporting
if $in is a filehandle

=back

=cut

sub mktemplate {
	my ($infile, $outfile, $infilename) = @_;
	my ($in, $out);
	my @close;

	if (!$infile) {
		$in = \*STDIN;
		$infilename ||= "<stdin>";
	} elsif (ref($infile) eq "GLOB") {
		$in = $infile;
	} else {
		open($in, "<", $infile) or die "can't open $infile: $!\n";
		push(@close, $in);
		$infilename ||= $infile;
	}
	if ($infilename) {
		$infilename = "$infilename:";
	} else {
		$infilename = "";
	}
	if (!$outfile) {
		$out = \*STDOUT;
	} elsif (ref($outfile) eq "GLOB") {
		$out = $outfile;
	} else {
		open($out, ">", $outfile) or die "can't open $outfile: $!\n";
		push(@close, $out);
	}

	# sanity check
	if (!feature_supported("_verbose", cfg_get_array('crf-features'))) {
		die "error in configuration\n";
	}
	my $i = 1;
	my %hash = map { $_ => $i++ } cfg_get_array('crf-features');

	my $counter = 1;
	while (<$in>) {
		s/\$n/$counter++/eg;
		s/^\s*(U[^:]+):\%x\[([^]]+):([^]]+)\]/features2num($1, $2, $3, $infilename, \%hash)/eg;
		print $out $_;
	}

	foreach my $fh (@close) {
		close($fh);
	}
}

