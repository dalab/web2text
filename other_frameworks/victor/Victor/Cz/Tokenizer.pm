#!/usr/bin/perl

package Victor::Cz::Tokenizer;
use Exporter;
@ISA = qw (Exporter);
@EXPORT = qw (&tokens);
################################################################################
# importovane moduly a pouzita pragmata:
# utf8 - pragma; zdrojovy kod je napsan v tomto kodovani
# Encode - modul; prevody mezi kodovanimi 
################################################################################
use strict;
use warnings;
use utf8;
use Encode;


my $datadir = $INC{"Victor/Cz/Tokenizer.pm"} || "./Tokenizer.pm";
$datadir =~ s/Tokenizer\.pm$//;

open (LETTERS, "$datadir/letters.dat") or die "Can't find file letters.dat with list of all recognized letters.\n";
local $_ = <LETTERS>; $_ = decode ("utf8", $_); chomp;
my $lower = $_;
$_ = <LETTERS>; $_ = decode ("utf8", $_); chomp;
my $upper = $_;
close (LETTERS);
my $regexp;
################################################################################
# exportovana procedura tokens (inputfile, outputfile, - povinne
#                               input kodovani, output kodovani - volitelne,
#                               implicitni hodnota = "utf8")
# zpracuje plain text podle sgtml, pripravi text pro segmentaci
################################################################################
sub tokens ($;$$) {
  ##############################################################################
  # inicializace - zpracovani parametru procedury, otevreni souboru
  ##############################################################################
  my ($input, $separator, $enter) = @_;

  (length($input)) or die "Write at least one argument input text.\n";
  $separator ||= "\n\n";
  $enter ||= 0; 
  
  my $output = "<csts lang=cs>\n<doc>\n<a>\n</a>\n<c>\n"; # hlavicka
  my $n = 0; # pocitadlo odstavcu
  my $t = 0; # pocitadlo tokenu
  
  ##############################################################################
  # hlavni program
  ##############################################################################
  my @input = split(/$separator/, $input);
  for (my $i = 0; $i <= $#input; $i++) {   
    local $_ = $input[$i];
    $n++;
    $output .= "<p n=$n>\n";
    $_ =~ s/^ +//;      # zruseni uvodnich mezer
    $_ =~ s/\n+/ \n /g; # nahrazeni "\n" za " \n ", aby se nevytvarel token <D> pred a po tokenu <enter>,
                        # ktery slouzi jako pomocne misto pro vlozeni zacatku vety 
    $_ =~ s/ +/ /g;     # zruseni opakujicich se mezer
    $regexp = "([^0123456789" . $lower . $upper . "])";
    my @veta = split(/$regexp/); # rozdeleni na slova podle nepismennych znaku
    
    # predzpracovani textu, konkretne spojeni ciselnych formatu do jednoho tokenu
    my $j = 0;
    for(my $k = 0; $k <= $#veta; $k++) {    
      # nutne nezpracovat prazdny retezec kvuli namatchovani nepismen na zacatku retezce nebo mezi dvema nepismenymi znaky 
      # split(/(:)/,"::a:") odpovida "" ":" "" ":" "a" ":"
      if($veta[$k] eq "") {
        next;
      }
      # cislo, kteremu bez mezery predchazi znamenko spoji do jednoho tokenu
      if($k+1 <= $#veta and $veta[$k] =~ /^[+-]$/ and $veta[$k+1] =~ /^[0-9]+$/) {
        $veta[$k+1] = $veta[$k] . $veta[$k+1];
        $k++;
      }
      # desetinne cislo zapsano bez mezery za carkou spoji do jednoho tokenu
      if($k+2 <= $#veta and $veta[$k] =~ /^[+-]?[0-9]+$/ and $veta[$k+1] =~ /^,$/ and $veta[$k+2] =~ /^[0-9]+$/) { 
        $veta[$k+2] = $veta[$k] . $veta[$k+1] . $veta[$k+2];
        $k += 2;
      }
      $veta[$j] = $veta[$k];
      $j++;
    }
    $#veta = $j-1;  
    
    for(my $k = 0; $k <= $#veta; $k++) {        
      if($veta[$k] =~ /^[+-]?[0-9]+(,[0-9]+)?$/) { # test na cislo
        $output .= "<f num>$veta[$k]\n";
        $t++;
      }
      elsif($veta[$k] =~ /^\n$/) {
        if ($enter == 0) { $output .= "<enter>\n"; }
      }
      else {
        $regexp = "[^0123456789" . $lower . $upper . "]";
        if($veta[$k] =~ /$regexp/) { # jedna se o nepismenne znaky
          $output .= "<d>$veta[$k]\n";
          $t++;
        }
        else {
          $regexp = "^[" . $upper . "]{2,}\$";
          if($veta[$k] =~ /$regexp/) { # cely token uppercase
            $output .= "<f upper>$veta[$k]\n";
            $t++;
          }
          else {
            $regexp = "^[" . $upper . "][" . $lower . "]*\$";
            if($veta[$k] =~ /$regexp/) { # prvni pismeno uppercase
              $output .= "<f cap>$veta[$k]\n";
              $t++;
            }
            else {
              $regexp = "^[" . $lower . "]+\$";
              if($veta[$k] =~ /$regexp/) { # cele slovo je lower case
               $output .= "<f>$veta[$k]\n";
               $t++;
              }
              elsif($veta[$k] =~ /[0-9]/) {
                $output .= "<f mixed>$veta[$k]\n"; # smes pismen a cislic
                $t++;
              }
              else {
                $output .= "<f>$veta[$k]\n"; # smes velkych a malych pismen
                $t++;
              }
            }
          }
        }
      }                           
      if($k < $#veta and $veta[$k+1] ne " ") { # tokeny nedeli mezera
        $output .= "<D>\n";
        $t++;
      }
      else {
        $k++; # je-li dalsi pole mezera, tak ji preskocim a nic nevypisuji
      } 
    } 
    $output .= "</p>\n";
  }

  $output .= "</c>\n</doc>\n</csts>\n"; # patka output souboru
  return ($output, $t, $n);
}

1;
