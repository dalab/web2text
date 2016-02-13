# $Id: Common.pm 356 2008-02-03 16:46:25Z michal $

package Victor::Common;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&is_blank);

sub is_blank {
	return ($_[0] =~ /^\s*$/);
}

1;
