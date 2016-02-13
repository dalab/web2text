#!/usr/bin/perl

package Victor::Cz::Cleaner;
use Exporter;
@ISA = qw (Exporter);
@EXPORT = qw (&cleans);

use strict;
use warnings;
use utf8;
use Encode;

sub cleans ($;$) {
  my ($input, $count) = @_;
  my $output = "";
  
  $count ||= 10; # count of words in line 

  my @all = split(/\n\n/, $input);
  for (my $j = 0; $j <= $#all; $j++) {  
    my @input = split(/\n/, $all[$j]);

    if (@input < 1) { next; }
    my $this = $input[0];
    $this =~ s/ +/ /g; # nahrazeni vice mezer jednou
    my @this = split(/ /, $this); # rozdeleni na slova
    $this = @this;
    
    for (my $i = 1; $i <= $#input; $i++) {    
      my $next = $input[$i]; 
      $next =~ s/ +/ /g; # nahrazeni vice mezer jednou      
      my @next = split(/ /, $next); # rozdeleni na slova
      
      if ( ($count <= $this) or ($count <= @next) ) {
         $output .= "$input[$i-1]\n";
      }
      $this = @next;
    }
    if ($count <= $this) { $output .= "$input[$#input]\n"; }
    
    $output .= "\n";
  }
  
  $output =~ s/\n{2,}/\n\n/g;
  $output =~ s/\n*$//; 
  return $output;
}

1;
