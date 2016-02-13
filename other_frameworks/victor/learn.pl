#!/usr/bin/perl -w
#
# $Id: learn.pl 377 2008-02-22 20:32:12Z michal $

=head1 NAME

learn.pl - learn from a set of annotated html documents

=head1 SYNOPSIS

learn.pl [--model model] [--template template] file ...


=head1 OPTIONS

=over

=item B<-m|--model model>

CRF++ model to output (defaults to the crf-model configuration variable)

=item B<-t|--template template>

CRF++ template to use (defaults to the crf-template configuration variable)

=back

=cut

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::Cfg;
use Victor::Output::CRF;
use Victor::MkTemplate;
use Victor::Temp;


my ($opt_model, $opt_template);
GetOptions(
	"_fileargs" => "1,",
	"m|model=s" => \$opt_model,
	"t|template=s" => \$opt_template,
);
$opt_model ||= cfg_get_string('crf-model');
$opt_template ||= cfg_get_string('crf-template');

my ($model, $template, $traindata);
$model = cfg_find_file("output model",
	$opt_model, "", "models", 1);
$template = create_template(cfg_find_file("template",
	$opt_template, "tpl", "templates"));

$traindata = convert_to_crf(2);
run_crf_learn($template, $traindata, $model);

verbose(1, "done\n");
exit 0;

sub create_template {
	my $infile = shift;
	
	verbose(1, "creating CRF++ template from $infile\n");
	my ($fh, $template) = tempfile();
	mktemplate($infile, $fh);
	close($fh);
	if ($opt_verbose >= 3) {
		system("cat", $template);
	}
	return $template;
}

sub convert_to_crf {
	my $train_opt = shift;
	verbose(1, "converting annotated HTML files to CRF++ format\n");
	my ($fh, $file) = tempfile();
	output_crf({train => $train_opt, output => $fh}, @ARGV);
	close($fh);
	return $file;
}

sub run_crf_learn {
	my ($template, $traindata, $model) = @_;
	verbose({
		1 => "running crf_learn\n",
		2 => "running crf_learn --textmodel $template $traindata $model\n"
	});
	my $err = system("crf_learn", "--textmodel", $template, $traindata,
				$model);
	if ($err == -1) {
		die "cannot execute crf_learn: $!\n";
	}
	if ($err) {
		print STDERR "crf_learn returned error\n";
		exit $err >> 8;
	}
}
