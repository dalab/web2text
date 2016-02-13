#!/usr/bin/perl -w
#
# $Id: xcheck.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

xcheck.pl - run cross-validation check

=head1 SYNOPSIS

xcheck.pl [--numgroups num] [--directory dir] [--template template]
          [--max-size size] file ...

=head1 OPTIONS

=over

=item B<-n|--numgroups num>

Number of group to divide the input files into. There will be one run for each
group, learning on the files from other groups and cleaning the group.

=item B<-d|--directory dir>

Directory where to save results. It's recommended to use a different directory
for each cross-check.

=item B<-t|--template template>

CRF++ template file to use.

=back

=cut

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::SplitHTML;
use Victor::Temp;
use Victor::LoadCRF;

use File::Basename;

my $numgroups = 4;
my $directory = ".";
my $template = "";
my $maxsize = 0;


# array of arrays of files
my @groups;

GetOptions("_fileargs" => "1,",
	"n|numgroups=i" => \$numgroups,
	"d|directory=s" => \$directory,
	"t|template=s" => \$template);

mkdirs($directory);
system("rm", "-rf", "$directory/clean", "$directory/run");
mkdirs("$directory/clean", "$directory/run");

for (my $i = 0; $i < @ARGV; $i++) {
	my $g = $i % $numgroups;
	$groups[$g] ||= [];
	push(@{$groups[$g]}, $ARGV[$i]);
}


open(my $fh, ">", "$directory/groups.txt") or die "Can't open `$directory/groups.txt': $!\n";
foreach (my $g = 0; $g < $numgroups; $g++) {
	foreach my $f (@{$groups[$g]}) {
		print $fh "$g $f\n";
	}
}
close($fh);

open(my $report, ">", "$directory/report.txt") or die "Can't open `$directory/report.txt': $!\n";
print $report "file\tok\ttext\ttotal\n";
my %total_score;

for (my $g = 0; $g < $numgroups; $g++) {
	my %group_score;
	my @args = ("-v", "-m", "$directory/model$g");
	if ($opt_config) {
		push(@args, "--config", $opt_config);
	}

	print "[$g] learning...\n";
	my @learn_args = @args;
	if ($template) {
		push(@learn_args, "-t", $template);
	}
	for (my $g2 = 0; $g2 < $numgroups; $g2++) {
		next if $g2 == $g;
		push(@learn_args, @{$groups[$g2]});
	}
	run_tool("./learn.pl", @learn_args);

	print "[$g] cleaning...\n";
	my @clean_args = (@args, "-o", "$directory/clean",
		"--format", "crf", @{$groups[$g]});
	run_tool("./clean.pl", @clean_args);

	print "[$g] scoring...\n";
	foreach my $ref (@{$groups[$g]}) {
		# copied from clean.pl
		my ($basename, $dir, $s) = fileparse($ref, qr/\.html?/);
		my $clean = "$directory/clean/$basename.clean.crf";
		print "$ref vs. $clean\n";
		my %score = scorer(clean => $clean, "ref" => $ref);
		print $report "$basename$s";
		foreach my $type (sort(keys(%score))) {
			print $report "\t$type: $score{$type}";
			$group_score{$type} += $score{$type};
			$total_score{$type} += $score{$type};
		}
		print $report "\n";
	}
	print $report "\n";
	print $report "group$g";
	foreach my $type (sort(keys(%group_score))) {
		print $report "\t$type: $group_score{$type}";
	}
	print $report "\n";
}

print $report "\n";
print $report "total";
foreach my $type (sort(keys(%total_score))) {
	print $report "\t$type: $total_score{$type}";
}
print $report "\n";
close($report);


sub mkdirs {
	foreach my $d (@_) {
		next if -d $d;
		mkdir($d) or die "Can't create directory `$d': $!\n";
	}
}

sub run_tool {
	system(@_);
	if ($? != 0) {
		my $e = $? >> 8;
		print "*** $_[0]: error $e\n";
		exit($e);
	}
}

sub scorer {
	my %opt = @_;
	my (@ref, @clean, %score);
	split_html($opt{"ref"}, \@ref);
	load_crf($opt{clean}, \@clean);
	if (scalar(@ref) != scalar(@clean)) {
		die "$opt{ref} and $opt{clean} have different length\n";
	}
	for (my $i = 0; $i < scalar(@ref); $i++) {
		$score{$ref[$i]->{class} . "/" . $clean[$i]->{class}}++;
	}
	return %score;
}

