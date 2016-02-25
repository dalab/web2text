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

=item B<-m|--max-file size>

Maximum size of files (in kilobytes) that will be scored using the
cleaneval.prl scorer. The fast diff-based scorer is allways run.

=back

=cut

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::SplitHTML;
use Victor::Output::Cleaneval;
use Victor::CLEANEVAL::Scorer;

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
	"t|template=s" => \$template,
	"m|maxsize=i" => \$maxsize);

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
print $report "file\tdiff\ttext\tmarkup\ttotal\n";
my $total_levenshtein = 0;
my %total_score = (
	diff => 0,
	text => 0,
	markup => 0,
	total => 0);

for (my $g = 0; $g < $numgroups; $g++) {
	my $group_levenshtein = 0;
	my %group_score = (
		diff => 0,
		text => 0,
		markup => 0,
		total => 0);
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
		"--format", "cleaneval", @{$groups[$g]});
	run_tool("./clean.pl", @clean_args);

	print "[$g] scoring...\n";
	foreach my $ref (@{$groups[$g]}) {
		# copied from clean.pl
		my ($basename, $dir, $s) = fileparse($ref, qr/\.html?/);
		my $clean = "$directory/clean/$basename.clean.txt";
		print "$ref vs. $clean\n";
		my %score = scorer(clean => $clean, "ref" => $ref,
				directory => "$directory/run/$basename$s",
				maxsize => $maxsize * 1024);
		print $report "$basename$s";
		for my $type (qw(diff text markup total)) {
			if (exists($score{$type})) {
				printf $report "\t\%.2f", $score{$type};
				$group_score{$type} += $score{$type};
				$total_score{$type} += $score{$type};
			} else {
				print $report "\t-";
			}
		}
		print $report "\n";
		if (exists($score{total})) {
			$total_levenshtein++;
			$group_levenshtein++;
		}
	}
	print $report "\n";
	print $report "group$g";
	printf $report "\t\%.2f", $group_score{diff} / scalar(@{$groups[$g]});
	for my $type (qw(text markup total)) {
		if ($group_levenshtein) {
			printf $report "\t\%.2f",
				$group_score{$type} / $group_levenshtein;
		} else {
			print $report "\t-";
		}
	}
	print $report "\n";
}

print $report "\n";
print $report "total";
printf $report "\t\%.2f", $total_score{diff} / scalar(@ARGV);
for my $type (qw(text markup total)) {
	if ($total_levenshtein) {
		printf $report "\t\%.2f",
			$total_score{$type} / $total_levenshtein;
	} else {
		print $report "\t-";
	}
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
