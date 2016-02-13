#!/usr/bin/perl

package Victor::Cz::LangID1;
use Exporter;
@ISA = qw (Exporter);
@EXPORT = qw (&recognize1);


use strict;
use warnings;
use utf8;
use Encode;
use Math::BigFloat;

my $datadir = $INC{"Victor/Cz/LangID1.pm"} || "./LangID1.pm";
$datadir =~ s/LangID1\.pm$//;

my (%cze, @sorted);
init (\%cze, \@sorted);
################################################################################
# nacteni modelu ceskeho jazyka
################################################################################
sub init {
  my ($model, $sorted) = @_;
  
  open(MODEL, "$datadir/cze_top400.lm") or die $!;
  while(<MODEL>) {
    chomp;
    local $_ = decode("utf8", $_);
    my ($a, $b) = split;
    $$model{$a} = $b;
  }
  close (MODEL);
  @$sorted = sort { $$model{$b} <=> $$model{$a} } keys %$model;
}
################################################################################

################################################################################
# vytvoreni jazykoveho modelu
################################################################################
sub create_language_model {
  my ($model, $input) = @_;
  my $count = Math::BigFloat->bzero();
  
  my @input = split(/\n/, $input);
  for (my $i = 0; $i <= $#input; $i++) {
    local $_ = $input[$i];
    $_ =~ s/[ 0-9\t\r\f.,!?;:"'--–=*%&()_%@#\$~`°{}[\]§]/_/g; # znaky, ktere se bezne vyskytuji v ceskem textu
    $_ =~ tr/AÁBCČDĎEÉĚFGHIÍJKLMNŇOÓPQRŘSŠTŤUÚŮVWXYÝZŽ/aábcčdďeéěfghiíjklmnňoópqrřsštťuúůvwxyýzž/;
    $_ =~ s/[^aábcčdďeéěfghiíjklmnňoópqrřsštťuúůvwxyýzž_]/*/g;
    $_ = "_" . $_ . "_";
    $_ =~ s/_+/_/g;

    my $length = length($_);
    my $pointer = $length;
    for (my $j = 0; $j < $length; $j++) {
      if ($pointer > 2) { $$model{substr($_, $j, 3)}++; }
      if ($pointer > 1) { $$model{substr($_, $j, 2)}++; }
      $$model{substr($_, $j, 1)}++;
      $pointer--;
    }
    $count->badd($length);
  }
  
  $count = 1 / $count;
  foreach my $key (keys %$model) {
    $$model{$key} *= $count; 
  } 
  return 0;
}
################################################################################

################################################################################
# vzdalenost jazykoveho modelu
################################################################################
sub difference {
  my ($cze, $unknown, $sorted, $max) = @_;

  my ($diff, $temp, $add) = (0, 0, 0);
  for (my $i = 0; $i < $max; $i++) {
    if (exists $$unknown{$$sorted[$i]}) {
      $temp = $$cze{$$sorted[$i]};
      $add = abs ($temp - $$unknown{$$sorted[$i]}) / $temp;
      if ($add > 1) { $add = 1; }
      $diff += $add;
    }
    else { 
      $diff += 1;
    }
  }
  if ($max < 1) { return 1; }
  return $diff / $max;
}
################################################################################
sub recognize1 {
  my ($input) = @_;
  my %unknown;
  
  create_language_model (\%unknown, $input);
  my $i = keys %unknown;
  $i = ($i >= 3200) ? 400 : $i / 8;
  return sprintf("%.5f", difference (\%cze, \%unknown, \@sorted, $i));
}

1;
