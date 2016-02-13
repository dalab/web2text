# $Id: Scorer.pm 382 2008-02-26 20:42:30Z michal $

package Victor::CLEANEVAL::Scorer;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(scorer);

use Victor::Output::Cleaneval;
use Victor::SplitHTML;

use File::Copy;
use Cwd;

sub scorer {
	my %opt = @_;
	my $clean = $opt{clean};
	my $ref = $opt{"ref"};
	my $directory = $opt{directory};
	my %score;

	if (! -d $directory) {
		mkdir($directory) or die "can't mkdir `$directory': $!\n";
	}

	copy("cleaneval.prl", "$directory/cleaneval.prl") or die "Can't copy `cleaneval.prl' to $directory: $!\n";
	chmod(0755, "$directory/cleaneval.prl");
	_copy_file($clean, "$directory/clean.txt");
	_copy_file($ref, "$directory/ref.txt");

	# FIXME: this sucks
	my $oldcwd = getcwd();
	chdir($directory);
	
	_cleaneval_scorer(\%score, $opt{maxsize});
	_diff_scorer(\%score);

	chdir($oldcwd);

	return %score;
}

# run the cleaneval scorer
sub _cleaneval_scorer {
	my ($score, $maxsize) = @_;

	if ($maxsize) {
		my @buf = stat("clean.txt");
		if ($buf[7] > $maxsize) {
			print STDERR "cleaned file too large ($buf[7] bytes), skipping\n";
			return;
		}
		@buf = stat("ref.txt");
		if ($buf[7] > $maxsize) {
			print STDERR "reference file too large ($buf[7] bytes), skipping\n";
			return;
		}
	}

	system("echo e | ./cleaneval.prl clean.txt ref.txt >/dev/null");
	if ($? != 0) {
		my $e = $? >> 8;
		print "*** cleaneval.prl: error $e\n";
		exit($e);
	}
	open(my $fh, "<", "clean_LOG.txt");
	while (my $line = <$fh>) {
		if ($line =~ /^Score for Text and Markup Scoring: ([0-9\.]*)%/) {
			$score->{markup} = $1;
		} elsif ($line =~ /^Score for Text Only Scoring: ([0-9\.]*)%/) {
			$score->{text} = $1;
		} elsif ($line =~ /^Final Overall score: ([0-9\.]*)%/) {
			$score->{total} = $1;
		}
	}
	close($fh);
}

# run a simple diff-based scorer
sub _diff_scorer {
	my $score = shift;

	my $numlines = 0;
	my $numedits = 0;
	$numlines += _tokenize("clean.txt", "clean.tok");
	$numlines += _tokenize("ref.txt", "ref.tok");
	open(my $pipe, "diff clean.tok ref.tok |") or die "Can't execute diff: $!\n";
	while (my $line = <$pipe>) {
		if ($line =~ /^[<>]/) {
			$numedits++;
		}
	}
	close($pipe);
	if (!$numlines) {
		$score->{diff} = 100;
	} else {
		$score->{diff} = ($numlines - $numedits) * 100 / $numlines;
	}
}

sub _copy_file {
	my ($from, $to) = @_;
	# if the file is html, need to convert it
	my $is_html = 0;
	open(my $fh, "<", $from) or die "Can't open `$from': $!\n";
	while (my $line = <$fh>) {
		next if $line =~ /^\s*$/;
		if ($line =~ /<\s*(!DOCTYPE|x?html|\?xml)\b/i) {
			$is_html = 1;
		}
		last;
	}
	close($fh);
	if ($is_html) {
		my @blocks;
		split_html($from, \@blocks);
		output_cleaneval({output => $to}, \@blocks);
	} else {
		copy($from, $to) or die "Can't copy `$from' to `$to': $!\n";
	}
}

sub _tokenize {
	my ($infile, $outfile) = @_;
	my $numlines = 0;
	open(my $in, "<", $infile) or die "Can't open `$infile': $!\n";
	open(my $out, ">", $outfile) or die "Can't open `$outfile': $!\n";
	while (my $line = <$in>) {
		$line =~ s/\s+/\n/g;
		$line =~ s/\b/\n/g;
		$line =~ s/\n+/\n/g;
		$line =~ s/^\n//;
		for (my $i = 0; $i < length($line); $i++) {
			if (substr($line, $i, 1) eq "\n") {
				$numlines++;
			}
		}
		print $out $line;
	}
	return $numlines;
}

1;
