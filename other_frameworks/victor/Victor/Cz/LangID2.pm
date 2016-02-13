#!/usr/bin/perl

package Victor::Cz::LangID2;
use Exporter;
@ISA = qw (Exporter);
@EXPORT = qw (&recognize2);


use strict;
use warnings;
use utf8;
use Encode;

my $datadir = $INC{"Victor/Cz/LangID2.pm"} || "./LangID2.pm";
$datadir =~ s/LangID2\.pm$//;

my %model;
init(\%model);

################################################################################
# nacteni modelu ceskeho jazyka
################################################################################
sub init {
  my ($model) = @_;
  open(MODEL, "$datadir/__cze__.lm") or die $!;
  my ($a, $b);
  while(<MODEL>) {
    local $_ = decode("utf8", $_);
    chomp;
    ($a, $b) = split;
    $$model{$a} = $b;
  }
  close MODEL;
}
################################################################################

sub recognize2 {
  my ($input, $treshold) = @_;
  $treshold ||= -2.1;
  my ($all_words, $good_words) = (0, 0);  
  
  local $_ = $input;
  $_ =~ s/[ 0-9\t\r\f\n.,!?;:"'--–=*%&()_%@#\$~`°{}[\]§]/_/g; # toto je spravne
  $_ =~ tr/AÁBCČDĎEÉĚFGHIÍJKLMNŇOÓPQRŘSŠTŤUÚŮVWXYÝZŽ/aábcčdďeéěfghiíjklmnňoópqrřsštťuúůvwxyýzž/;
  $_ =~ s/[^aábcčdďeéěfghiíjklmnňoópqrřsštťuúůvwxyýzž_]+/*/g;
  $_ =~ s/_+/_/g;

  my @slova = split(/_/, $_);
  for (my $j = 0; $j <= $#slova; $j++) {
    my $slovo = "__" . $slova[$j] . "__";
    my $n = 0;
    my $suma = 0;
    my $length = length($slovo) - 2;
    for (my $i = 0; $i < $length; $i++) {    
      my $str3 = substr($slovo, $i, 3);
      if (exists $model{$str3}) {
        my $str2 = substr($str3, 0, 2);
        my $add = $model{$str3} - $model{$str2} + -0.1288180216105;
        $suma += $add;
      }
      else {
        $suma += -21.0917592625881;  # maximum3 -21,0917592625881 maximum2 -21,2205772841986
      }
      $n++;
    }
    if ($n > 0) {
      $suma /= $n;
      if ($suma < $treshold) { $good_words++; }
      $all_words++;
    }
  }
  
  if ($all_words == 0) { return "1.00000"; }
  return sprintf("%.5f", $good_words / $all_words);
}

1;
