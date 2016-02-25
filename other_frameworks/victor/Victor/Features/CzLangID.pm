# $Id$

package Victor::Features::CzLangID;

use strict;
use warnings;
use open ':locale';

use Victor::Cfg;
# FIXME: load these on demand...
use Victor::Cz::LangID1;
use Victor::Cz::LangID2;
use Victor::Features::Util;

sub calculate_features {
	my ($block, $fv) = @_;
	foreach my $i (qw(1 2)) {
		no strict 'refs';
		my $id = &{"recognize$i"}($block->{text});
		if ($id > 1) {
			$id = 1.0;
		}
		$fv->{"langid$i"} = scale($id * 100, 100, "langid$i");
	}


}

sub feature_supported {
	return $_[0] =~ /^langid[12]$/;
}

1;
