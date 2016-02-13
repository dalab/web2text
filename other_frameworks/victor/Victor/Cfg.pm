# $Id: Cfg.pm 383 2008-02-27 21:41:59Z michal $

=head1 NAME

Victor::Cfg

=head1 DESCRIPTION

Interface to Victor configuration

=cut

package Victor::Cfg;

use strict;
use warnings;
use open ':locale';

use Exporter;
use vars qw(@ISA @EXPORT);
@ISA = qw(Exporter);
@EXPORT = qw(&load_cfg &cfg_find_file &cfg_get_set &cfg_contains &cfg_get_array
	&cfg_get_string &cfg_get_int &cfg_isset
	&cfg_tag_in_class &cfg_tag_classes &cfg_all_tags
	&cfg_list_variables);

use Victor::Expand;

# package-global symbols start with uppercase letter
#
my @Configdirs = qw(/etc/victor/configs);
my $me = $INC{"Victor/Cfg.pm"};
$me =~ s/Victor\/Cfg\.pm$//;
$me ||= ".";
unshift @Configdirs, "$me/configs";
if ($ENV{"VICTOR_CONFIG_DIR"}) {
	unshift @Configdirs, $ENV{"VICTOR_CONFIG_DIR"};
}



# configuration variables
# sets - implemented as hashrefs {name => 1}
my %Sets = (
	'tag-delete' => {},
	'tag-ignore' => {},
	'class-split-0' => {},
	'class-split-1' => {},
	'class-split-2' => {},
);

# arrays - implemented as array refs
my %Arrays = (
	'block-classes' => [],
	'class-thresholds' => [],
	'feature-modules' => [],
	'crf-features' => [],
	'feature-word-count-values' => [],
	'feature-sentence-count-values' => [],
);

# strings / numbers - implemented as scalars (no refs)
my %Strings = (
	"crf-template" => "",
	"crf-model" => "",
	"task" => "",
);

# booleans - ditto
my %Bools = (
	'hide-ignored-tags' => 0,
	'hide-ignored-attribs' => 0,
);

my %Integers = ();

# inverted class-* sets
# tag => [ class1, class2 ]
my %Tag_classes = ();

# array all recognized tags
my $All_tags = [];

my $Cfg_loaded;

sub load_cfg {
	my ($filename) = shift;
	
	# reset config
	foreach my $k (keys(%Sets)) {
		$Sets{$k} = {};
	}
	foreach my $k (keys(%Arrays)) {
		$Arrays{$k} = [];
	}
	foreach my $k (keys(%Strings)) {
		$Strings{$k} = "";
	}
	foreach my $k (keys(%Bools)) {
		$Bools{$k} = 0;
	}
	_parse_file($filename, {
			'sets' => \%Sets,
			'arrays' => \%Arrays,
			'strings' => \%Strings,
			'bools' => \%Bools,
			'integers' => \%Integers});
	_check_disjoint_sets('tag-delete', 'tag-ignore');
	_check_disjoint_sets('class-split-0', 'class-split-1', 'class-split-2');

	_assign_classes();
	
	$Cfg_loaded = 1;
}

sub cfg_find_file {
	my ($what, $name, $suffix, $dirs, $dironly) = @_;
	my $res;
	if ($name =~ /\//) {
		$res = $name;
	} else {
		if ($suffix && $name !~ /\.$suffix$/) {
			$name .= ".$suffix";
		}
		if (ref($dirs) eq "") {
			$dirs = [$dirs];
		}
		foreach my $dir (@$dirs) {
			if ($dironly && -e $dir ||
			   !$dironly && -e "$dir/$name") {
				$res = "$dir/$name";
				last;
			}
		}
	}
	if (!$res || !$dironly && ! -e $res) {
		die "can't find $what $name\n";
	}
	return $res;
}

sub cfg_get_set {
	_autoload_cfg();
	my $res = $Sets{$_[0]};
	if (!defined($res)) {
		die "configuration error: set $_[0] not found, exiting\n";
	}
	return %$res;
}

sub cfg_contains {
	my %h = cfg_get_set($_[0]);
	return $h{$_[1]};
}

sub cfg_get_array {
	_autoload_cfg();
	my $res = $Arrays{$_[0]};
	if (!defined($res)) {
		die "internal error: configuration array $_[0] not found, exiting\n";
	}
	return @$res;
}

sub cfg_get_string {
	_autoload_cfg();
	my $res = $Strings{$_[0]};
	if (!defined($res)) {
		if (defined($Arrays{$_[0]})) {
			$res = join(" ", @{$Arrays{$_[0]}});
		} else {
			die "internal error: configuration string $_[0] not found, exiting\n";
		}
	}
	return $res;
}

sub cfg_get_int {
	_autoload_cfg();
	my $res = $Integers{$_[0]};
	if (!defined($res)) {
		die "internal error: configuration integer $_[0] not found, exiting\n";
	}
	return $res;
}

sub cfg_isset {
	_autoload_cfg();
	my $res = $Bools{$_[0]};
	if (!defined($res)) {
		die "internal error: configuration flag $_[0] not found, exiting\n";
	}
	return $res;
}

sub cfg_tag_in_class {
	_autoload_cfg();
	my ($tag, $class) = @_;
	my $classes = cfg_tag_classes($tag);
	foreach my $c (@$classes) {
		if ($c eq $class) {
			return 1;
		}
	}
	return 0;
}

sub cfg_tag_classes {
	_autoload_cfg();
	my $tag = shift;
	if (!exists($Tag_classes{$tag})) {
		_warn("unknown tag <$tag>, ignoring");
		return ();
	}
	return @{$Tag_classes{$tag}};
}

sub cfg_all_tags {
	_load_all_tags();
	return @$All_tags;
}

sub cfg_list_variables {
	my $re = shift;
	_autoload_cfg();
	return grep (/$re/, (keys(%Sets), keys(%Arrays), keys(%Strings),
		keys(%Bools)));
}

### internal subroutines #######
sub _autoload_cfg {
	if (!$Cfg_loaded) {
		eval {load_cfg("default.conf") };
		if ($@) {
			die $@ . "error loading configuration, exiting\n";
		}
	}
}

# make sure that $Sets{$_[0]}, $Sets{$_[1]}, ... are disjoint
sub _check_disjoint_sets {
	my ($i, $i2, @h);
	
	for ($i = 0; $i < @_; $i++) {
		$h[$i] = $Sets{$_[$i]} || {};
	}

	for ($i = 0; $i < scalar(@_); $i++) {
		for ($i2 = $i + 1; $i2 < scalar(@_); $i2++) {
			foreach my $k (keys(%{$h[$i]})) {
				if ($h[$i2]->{$k}) {
					die "error: $k given both in $_[$i] and $_[$i2]\n";
				}
			}
		}
	}
	return 1;
}

# load list of all tags to check the config
sub _load_all_tags {
	if (@$All_tags) {
		return 1;
	}
	my %arrays = ( 'elements' => [] );
	_parse_file("elements.conf", {'arrays' => \%arrays});
	$All_tags = $arrays{elements};
}

# fill %Tag_classes with tag -> ["class1", "class2"] pairs
sub _assign_classes {
	_load_all_tags();
	my @classes = grep { /^class-/ } keys(%Sets);
	foreach my $tag (@$All_tags) {
		$Tag_classes{$tag} = [];
		foreach my $class (@classes) {
			if ($Sets{$class}->{$tag}) {
				push(@{$Tag_classes{$tag}}, $class);
			}
		}
	}
}

# parse a file, return 1 on success
# $filename: file to parse (relative to $configdir)
# $ctx->{sets}: set variables to store
# $ctx->{arrays}: array variables to store
# $ctx->{strings}: string variables to store
# $ctx->{bools}: boolean variables to store
my @last_filenames;
sub _parse_file {
	my ($name, $ctx) = @_;
	my $filename = cfg_find_file("configuration file", $name, "conf", \@Configdirs);
	push(@last_filenames, $filename);
	$ctx->{sets} ||= {};
	$ctx->{arrays} ||= {};
	$ctx->{strings} ||= {};
	$ctx->{bools} ||= {};
	$ctx->{visited} ||= {};
	# FIXME: this is just a basic check
	if ($ctx->{visited}->{$filename}) {
		_warn("file $filename included twice (recursion?), skipping");
		return 1;
	}
	$ctx->{visited}->{$filename} = 1;
	my $fh;
	if (!open($fh, "<", $filename)) {
		die "can't open $filename: $!\n";
	}
	while (<$fh>) {
		chomp;
		s/#.*//;
		my $line = $_;
		my @line = split(/(?<!\\)\s+/, $line);
		next if !@line;
		my $token = shift @line;
		if (substr($token, -1) eq ":") {
			# variable definition
			$token = substr($token, 0, -1);
			if (exists($ctx->{sets}->{$token})) {
				_do_set($ctx->{sets}->{$token}, $ctx, @line);
			} elsif (exists($ctx->{arrays}->{$token})) {
				_do_array($ctx->{arrays}->{$token}, $ctx, @line);
			} elsif (exists($ctx->{bools}->{$token})) {
				_do_bool($ctx->{bools}->{$token}, @line);
			} elsif (exists($ctx->{strings}->{$token})) {
				_do_string($ctx->{strings}->{$token}, $line);
			# we allow arbitrary 'class-*' sets
			} elsif ($token =~ /^class-/) {
				$ctx->{sets}->{$token} ||= {};
				_do_set($ctx->{sets}->{$token}, $ctx, @line);
			# arbitrary feature-*-scale integers
			} elsif ($token =~ /^feature-.*-scale$/) {
				_do_int($ctx->{integers}->{$token}, @line);
			# and arbitrary feature-* arrays (ugly...)
			} elsif ($token =~ /^feature-/) {
				$ctx->{arrays}->{$token} ||= [];
				_do_array($ctx->{arrays}->{$token}, $ctx, @line);
			} else {
				_parse_error("unknown variable: $token");
			}
		} elsif ($token eq "include") {
			# include file
			if (scalar(@line) != 1) {
				_parse_error("'include' takes exactly one argument");
			}
			if (!_parse_file($line[0], $ctx)) {
				return 0;
			}
		} elsif ($token eq "undef") {
			# undef name ...
			_do_undef($ctx, @line);
		} else {
			_parse_error("unknown configuration command: $token");
		}
	}
	pop(@last_filenames);
	return 1;
}

# a $b c{0,1} => a <elements of set/array b> c0 c1
sub _expand_variables {
	my $ctx = shift;
	my @res = ();
	foreach my $elem (@_) {
		if (substr($elem, 0, 1) eq '$') {
			$elem = substr($elem, 1);
			if (exists($ctx->{sets}->{$elem})) {
				push(@res, keys(%{$ctx->{sets}->{$elem}}));
			} elsif (exists($ctx->{arrays}->{$elem})) {
				push(@res, @{$ctx->{arrays}->{$elem}});
			} else {
				_parse_error("unknown variable: $elem");
			}
		} else {
			eval {
				push(@res, expand_curly($elem));
				1;
			} or _parse_error($@);
		}
	}
	return @res;
}

# name: elem1 elem2 ... as set
sub _do_set {
	my $set = shift;
	my $ctx = shift;
	foreach (_expand_variables($ctx, @_)) {
		$set->{$_} = 1;
	}
}

# name: elem1 elem2 ... as array
sub _do_array {
	my $array = shift;
	my $ctx = shift;
	push (@$array, _expand_variables($ctx, @_));
}

# name: (0|false|no|off|1|true|yes|on)
sub _do_bool {
	if (@_ != 2) {
		_parse_error("boolean value expected");
	}
	if ($_[1] =~ /^(0|false|no|off)$/i) {
		$_[0] = 0;
	} elsif ($_[1] =~ /^(1|true|yes|on)$/i) {
		$_[0] = 1;
	} else {
		_parse_error("boolean value expected");
	}
}

# name: string
# we need to get the value from the original
# line to preserve whitespace
sub _do_string {
	my $line = $_[1];
	$line =~ s/.*?:\s+//;
	$_[0] = $line;
}

sub _do_int {
	if (@_ != 2 || $_[1] !~ /^[0-9]*$/) {
		_parse_error("integer value expected");
	}
	$_[0] = $_[1];
}

sub _do_undef {
	my $ctx = shift;
	foreach my $name (map(expand_curly($_), @_)) {
		if (exists($ctx->{sets}->{$name})) {
			$ctx->{sets}->{$name} = {};
		} elsif (exists($ctx->{arrays}->{$name})) {
			$ctx->{arrays}->{$name} = [];
		} elsif (exists($ctx->{bools}->{$name})) {
			$ctx->{bools}->{$name} = 0;
		} elsif (exists($ctx->{strings}->{$name})) {
			$ctx->{strings}->{$name} = "";
		} elsif ($name =~ /^(class|feature)-/) {
			# do nothing
		} else {
			_parse_error("unknown variable: $name");
		}
	}
}

sub _parse_error {
	die "$last_filenames[$#last_filenames]:$.: error: $_[0]\n";
};

sub _warn {
	print STDERR "warning: $_[0]\n";
}

1;
