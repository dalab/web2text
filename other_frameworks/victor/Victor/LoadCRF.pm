# $Id: LoadCRF.pm 383 2008-02-27 21:41:59Z michal $
=head1 NAME

Victor::LoadCRF

=head1 SYNOPSIS

load_crf($in, \@blocks)

=head1 DESCRIPTION

Loads output of crf_test

=cut

package Victor::LoadCRF;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&load_crf);

use Victor::Cfg;

sub load_crf {
	my ($in, $blocks) = @_;
	my $fh;
	if (ref($in) ne "GLOB") {
		open($fh, "<", $in) or die;
		$in = $fh;
	}
	my $numfeatures = scalar(cfg_get_array('crf-features'));
	my @thresholds = get_thresholds();
	while (<$in>) {
		chomp;
		next if /^(#|$)/;
		# input is 'id f1 f2 f3 tag2/0.80 tag1/0.15 tag2/0.80 tag3/0.05'
		my @line = split(/[\t\/]/);
		my $class;
		my %prob = splice(@line, $numfeatures + 3);
		my @candidates = sort {$prob{$b} <=> $prob{$a}} keys(%prob);

		# (tag1, op1, theshold1, ...)
		for (my $i = 0; $i < scalar(@thresholds); $i += 3) {
			my $cl = $thresholds[$i];
			my $op = $thresholds[$i + 1];
			my $th = $thresholds[$i + 2];
			my $p = $prob{$thresholds[$i]};
			# will be selected unless the probability is lower than
			# given threshold
			if ($op eq '<') {
				if ($p >= $th) {
					$class = $cl;
					last;
				}
			# will be skipped unless the probability is higher than
			# given thershold
			} else {
				if ($p <= $th) {
					$prob{$cl} = 0;
					@candidates = grep {$_ ne $cl} @candidates;
				}
			}
		}
		if (!defined($class)) {
			$class = $candidates[0];
		}
		$blocks->[$line[0]]->{class} = $class;
	}
	if ($fh) {
		close($fh);
	}
}

my @_thresholds;
my $_loaded;
sub get_thresholds {
	if ($_loaded) {
		return @_thresholds;
	}
	my @th = cfg_get_array('class-thresholds');
	@_thresholds = map {
		if (/(.*)([<>])(.*)/) {
			($1, $2, $3);
		} else {
			die "Invalid threshold syntax (expected label[<>]value): $_\n";
		}
	} @th;
	$_loaded = 1;
	return @_thresholds;
}

1;
