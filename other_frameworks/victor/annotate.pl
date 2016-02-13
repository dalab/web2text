#!/usr/bin/perl
#
# $Id: annotate.pl 356 2008-02-03 16:46:25Z michal $

=head1 NAME

annotate.pl - annotate html document for use with other Victor tools

=head1 SYNOPSIS

annotate.pl infile outfile

=head1 DESCRIPTION

This script is ugly. It redraws the whole screen on every keystroke. This is
certainly not an example of how a terminal program should behave...

=cut

use strict;
use warnings;
use open ':locale';

use Victor::Getopt;
use Victor::Cfg;
use Victor::SplitHTML;
use Victor::Output::HTML;
use Victor::Temp;

use Curses;
use List::Util qw(min max);
use Encode;
use File::Copy;


GetOptions("_fileargs" => "1,2");

my $infile = $ARGV[0];
my $title = "annotate: $infile";
my $outfile = $ARGV[1] || $infile;
my @blocks;

# index of the highl:ighted block
my $cursor = 0;

# index of the block displayed on the top
my $view_pos = 0;

# index of the selection start (-1 means no selection)
my $selection = -1;

# last search string
my $search_string = "";

# size of the main window
my ($mainlines, $maincols);

# number of visible blocks
my $visible_blocks;

# class name <-> shortcut mapping
my %class2key = ();
my %key2class = (
	# these are allways there and are handled specially
	# make sure the loop below doesn't use them
	'q' => undef,  # quit
	'w' => undef,  # write
	's' => undef,  # selection start / cancel
	' ' => undef,  # clear class
	'/' => undef,  # search
	'n' => undef,  # next
);
foreach my $class (cfg_get_array('block-classes')) {
	my $i = 0;
	my $key;
	do {
		$key = substr($class, $i, 1);
		$i++;
	} while (exists($key2class{$key}) && $i < length($class));
	$i = ord('a');
	while (exists($key2class{chr($i)}) && $i < ord('z')) {
		$i++;
	}
	if (exists($key2class{chr($i)})) {
		print STDERR "No shortcut for $class available, oh well.\n";
		next;
	}
	$class2key{$class} = $key;
	$key2class{$key} = $class;
}

split_html(\$infile, \@blocks);
if (!@blocks) {
	die "$infile: didn't recognize input\n";
}

initscr();
$mainlines = $LINES - 1;
$maincols = $COLS - 4;
noecho();
cbreak();

# set xterm title
if ($ENV{'TERM'} =~ /^xterm|^rxvt/) {
	$| = 1;
	print "\e]0;$title\a";
	$| = 0;
}

my $main = newwin($mainlines, $maincols, 0, 4);
my $left = newwin($mainlines, 4, 0, 0);
my $bottom = newwin(1, $COLS, $mainlines, 0);
$bottom->keypad(1);

while (1) {
	# FIXME: don't repaint every time
	repaint();
	my $key = $bottom->getch();
	if ($key eq KEY_UP || $key eq 'k') {
		scroll_up();
	} elsif ($key eq KEY_DOWN || $key eq 'j') {
		scroll_down();
	} elsif ($key eq KEY_PPAGE) {
		scroll_up($visible_blocks >> 1);
	} elsif ($key eq KEY_NPAGE) {
		scroll_down($visible_blocks >> 1);
	} elsif ($key eq ' ' || defined($key2class{$key})) {
		# space means clear the class
		my $class = ($key eq ' ') ? undef : $key2class{$key};
		if ($selection == -1) {
			$blocks[$cursor]->{class} = $class;
			scroll_down();
		} else {
			for (my $i = min($cursor, $selection);
				$i <= max($cursor, $selection);
				$i++) {
				$blocks[$i]->{class} = $class;
			}
			$selection = -1;
		}
	} elsif ($key eq '/') {
		search();
	} elsif ($key eq 'n') {
		search_next();
	} elsif ($key eq 's') {
		$selection = $cursor;
	} elsif ($key eq 'q') {
		quit();
	} elsif ($key eq 'w') {
		save();
	} elsif ($key eq "\t") {
		my $i;
		for ($i = ($cursor + 1) % @blocks; $i != $cursor;
			$i = ($i + 1) % @blocks) {
			if (!defined($blocks[$i]->{class})) {
				last;
			}
		}
		if ($i == $cursor) {
			beep();
		} else {
			$cursor = $i;
			adjust_view();
		}
	} else {
		print STDERR "key: \"$key\" (" . ord($key) . ")\n";
		beep();
	}
}

sub scroll_up {
	my $lines = (shift || 1);
	if ($cursor > 0) {
		$cursor = max(0, $cursor - $lines);
		adjust_view();
	}
}

sub scroll_down {
	my $lines = (shift || 1);
	if ($cursor < @blocks - 1) {
		$cursor = min(@blocks - 1, $cursor + $lines);
		adjust_view();
	}
}

# FIXME: this is too simple
sub adjust_view {
	if ($view_pos > $cursor - 3) {
		$view_pos = max(0, $cursor - 3);
	}
	if ($view_pos + $visible_blocks - 1 < $cursor + 3) {
		$view_pos = min(
			$cursor + 3 - ($visible_blocks - 1),
			@blocks - 1 - ($visible_blocks - 1));
	}
}

sub search {
	my $first_key = 1;
	while (1) {
		$bottom->clear();
		$bottom->addstr("Search: $search_string");
		$bottom->refresh();
		my $key = $bottom->getch();
		if ($key eq "\n") {
			last;
		} elsif ($key eq KEY_BACKSPACE) {
			if (length($search_string) > 0) {
				if ($first_key) {
					$search_string = "";
				} else {
					chop($search_string);
				}
			} else {
				beep();
			}
		} else {
			if ($first_key) {
				$search_string = $key;
			} else {
				$search_string .= $key;
			}
		}
		$first_key = 0;
	}
	if (!$search_string) {
		return;
	}
	do_search();
}

sub search_next {
	if ($search_string) {
		do_search();
	} else {
		search();
	}
}

sub do_search {
	for (my $i = $cursor + 1; $i < @blocks; $i++) {
		if (index(lc($blocks[$i]->{text}), lc($search_string)) != -1) {
			scroll_down($i - $cursor);
			return;
		}
	}
	for (my $i = 0; $i < $cursor; $i++) {
		if (index(lc($blocks[$i]->{text}), lc($search_string)) != -1) {
			scroll_up($cursor - $i);
			return;
		}
	}
	beep();
}

sub repaint {
	$main->clear();
	$left->clear();
	$bottom->clear();
	my ($x, $y);
	$visible_blocks = 0;
	for (my $i = $view_pos; $i < @blocks; $i++) {
		if ($i == $cursor) {
			$main->attrset(A_STANDOUT);
			$left->attrset(A_STANDOUT);
			print_block($blocks[$i]);
			$main->attrset(A_NORMAL);
			$left->attrset(A_NORMAL);
		} elsif ($selection != -1 && within($i, $cursor, $selection)) {
			$main->attrset(A_UNDERLINE);
			$left->attrset(A_UNDERLINE);
			print_block($blocks[$i]);
			$main->attrset(A_NORMAL);
			$left->attrset(A_NORMAL);
		} else {
			print_block($blocks[$i]);
		}
		$visible_blocks++;
		if ($visible_blocks >= $mainlines >> 1) {
			if ($visible_blocks > $mainlines >> 1) {
				print STDERR "BUG: visible_blocks ($visible_blocks) > mainlines / 2 (" . ($mainlines >> 1) . ")\n";
			}
			last;
		}
	}
	$bottom->addstr(get_prompt());
	$main->refresh();
	$left->refresh();
	$bottom->refresh();
}

sub within {
	my ($x, $a, $b) = @_;
	return ($x >= min($a, $b) && $x <= max($a, $b));
}

my $_shortcuts;
sub get_prompt {
	my $prompt = "[Q]uit [W]rite";
	if ($selection == -1) {
		$prompt .= " [S]elect";
	} else {
		$prompt .= " un[S]elect";
	}
	if (!$_shortcuts) {
		$_shortcuts = "";
		foreach my $c (cfg_get_array('block-classes')) {
			$_shortcuts .= " [" . uc($class2key{$c}) . "]" .
				substr($c, 1);
		}
	}
	$prompt .= $_shortcuts . ": ";
}

sub print_block {
	my $block = shift;
	my ($x, $y);
	$main->getyx($y, $x);
	$left->move($y, 0);
	my $key = ' ';
	if (defined($block->{class})) {
		$key = $class2key{$block->{class}};
	}
	$left->addstr("[$key]");

	my $text = decode("utf8", $block->{text});
	$text =~ s/\n/\\n/gs;
	$text =~ s/\t/\\t/g;
	my $length = length($text);
	if ($length <= $maincols * 2) {
		$main->addstr("$text");
	} else {
		$main->addstr(substr($text, 0, $maincols - 3) . "...");
		$main->move($y + 1, 0);
		$main->addstr("..." . substr($text, -$maincols + 3));
	}
	$main->move($y + 2, 0);
}

sub save {
	my $tmpfile;
	my $fh;
	if ($infile eq $outfile) {
		($fh, $tmpfile) = tempfile(1);
	} else {
		open($fh, ">", $outfile);
	}
	output_html({orig => $infile, output => $fh}, \@blocks);
	close($fh);
	if ($infile eq $outfile) {
		move($tmpfile, $outfile);
	}
}

sub quit {
	save();
	exit;
}

sub cleanup {
	endwin;
};

BEGIN {
	$SIG{__DIE__} = \&cleanup;
}

END {
	cleanup();
}
