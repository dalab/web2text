# $Id: Temp.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Temp;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&tempfile);

use File::Temp;
use File::Spec;

my @unlink;
my $tmpdir = File::Spec->tmpdir();

sub tempfile {
	my $keep = shift;
	my ($fh, $name) =  File::Temp::tempfile("victor.XXXXXXXXXX",
		DIR => $tmpdir);
	binmode($fh, ':utf8');
	if (!$keep) {
		push(@unlink, $name);
	}
	return ($fh, $name);
}


END {
	foreach my $file (@unlink) {
		unlink($file);
	}
}

1;
