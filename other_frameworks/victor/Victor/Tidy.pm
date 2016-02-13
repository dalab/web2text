# $Id: Tidy.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Tidy;

use strict;
use warnings;
use open ':locale';
use vars qw(@ISA @EXPORT);

use Exporter;
@ISA = qw(Exporter);
@EXPORT = qw(tidy_html);

use Victor::Temp;

sub tidy_html {
	my ($file, $cfg) = @_;
	$cfg = "/dev/null" unless defined $cfg;
	# workaround a tidy bug / misfeature:
	# <script>document.write('<script>...');</script>
	# would be converted to
	# <script>document.write('<script>...');<\/script>
	#                                        ^
	# this introduces another bug by potentially mangling text which
	# shouldn't be mangled... *sigh*
	my ($fh_tmp, $tmp) = tempfile();
	open(my $fh_file, "<", $file) or return (undef, "can't open $file: $!");
	while (my $line = <$fh_file>) {
		$line =~ s/(\bdocument.write\s*\()([^)]+)/$1 . _split_script($2)/e;
		print $fh_tmp $line;
	}
	close($fh_file);
	close($fh_tmp);
	open(my $out, "-|", "tidy", "-config", $cfg, $tmp)
		or return (undef, "can't run tidy: $!");
	my @clean = <$out>;
	my $clean = join("\n", @clean);
	return ($clean, undef);
}

sub _split_script {
	my $text = shift;
	# we don't care about javascript code, so just mangle it so that tidy
	# won't get confused...
	$text =~ s/\bscript\b/scr ipt/g;
	return $text;
}

1;
