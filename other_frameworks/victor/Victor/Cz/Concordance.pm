#!/usr/bin/perl

package Victor::Cz::Concordance;
use Exporter;
@ISA = qw (Exporter);
@EXPORT = qw (&concos);
################################################################################
# importovane moduly a pouzita pragmata:
# utf8 - pragma; zdrojovy kod je napsan v tomto kodovani
# Encode - modul; prevody mezi kodovanimi 
################################################################################
use strict;
use warnings;
use utf8;
use Encode;

my $datadir = $INC{"Victor/Cz/Concordance.pm"} || "./Concordance.pm";
$datadir =~ s/Concordance\.pm$//;

open(QUOTES, "$datadir/quotes.dat") or die "Can't find file quotes.dat with list of quotation marks and right (closing) brackets.\n";
$_ = <QUOTES>; $_ = decode ("utf8", $_); chomp;
my $quotes = $_;
close (QUOTES);
################################################################################
# exportovana procedura concos (inputfile, outputfile, - povinne
#                               input kodovani, output kodovani - volitelne,
#                               implicitni hodnoty = "utf8",
#                               left context, right context - volitelne,
#                               implicitni hodnoty = 50)
# zobrazi text z formatu sgml, ukaze kontext kolem mist, kde muze byt konec vety
# zobrazi znak <s> tam, kde je konec vety a "   ", kde neni
################################################################################
sub concos ($;$$) {
  ##############################################################################
  # inicializace - zpracovani parametru procedury, otevreni souboru
  ##############################################################################
  my ($input, $l, $r) = @_;
  $l ||= 50;
  $r ||= 50;
  ($input) or die "Write at least one argument input text.\n";  
  ($l =~ /^\d+$/) or ($l < 1) or ($l > 200) or die "Wrong option value -l $l.\n";
  ($r =~ /^\d+$/) or ($r < 1) or ($r > 200) or die "Wrong option value -r $r.\n";
  $l *= -1;  # vynasobim -1 pro pozdejsi pouziti ve fci substr()
 
  my $output = ""; 
  my @input = split(/"<p n="/, $input);
  for (my $m = 0; $m <= $#input; $m++) {   
    $_ = $input[$m];
    
    my $gap = 0; #boolean, TRUE (1) ma se tisknout mezera
    my $lcontext = "";
    
    my @odstavec = split(/\n/); # rozdeleni podle tokenu
    for(my $i = 0; $i <= $#odstavec; $i++) {
      my $temp = $odstavec[$i]; # zkopiruji, budu totiz retezec modifikovat   
      ##########################################################################
      # case podle druhu tokenu
      ##########################################################################

      my $regexp = "<d>[.!?" . $quotes . "]";
      if (($temp =~ /$regexp/) or # potencialni konec vety, budu zobrazovat
         (($temp eq "<s>") and (($i <= 1) or not(($odstavec[$i-2] =~ /$regexp/) or ($odstavec[$i-2] =~ /abbr>/) or ($odstavec[$i-2] =~ /<\/p>/)))) or # odchytava <s> umisteny kvuli tokenu <enter> ci pocatku odstavce
         ($temp =~ /abbr>/)) { # musim zobrazit i tecku ve zkratce
        
        $output .= "[ ] "; # policko pro kontrolu
        
        if ($temp =~ /abbr>/) { # jedna se o zkratku
          $temp =~ s/<.*>//;
          $temp =~ s/\.//; # smazani tecky od zkratky
          if ($gap == 1) {
            $lcontext .= " $temp";
            $lcontext = substr($lcontext, $l);
          }
          else {
            $lcontext .= "$temp";
            $lcontext = substr($lcontext, $l);
          }           
          $temp = "."; # oznaceni tokenu jako tecky
          $gap = 0; # nebudu tisknout mezeru mezi zkratku a tecku
        }
        my $k = -1 * $l - length($lcontext); # doplneni leveho kontextu mezerami
        for (my $j = 0; $j < $k; $j++) { $output .= " "; }
        $output .= "$lcontext"; # tisk leveho kontextu
        
        if ($temp eq "<s>") { # misto interpunkcniho znaku vytisknu mezeru,
                         # aby to bylo stejne zarovnano 
          $output .= " " . " <s> "; 
        }
        else { # vypisu interpunkcni znamenko a podivam se, jestli napsat <s>
          $temp =~ s/<.*>//;
          $output .= $temp;
          if (($i < $#odstavec) and ($odstavec[$i+1] =~ /<\/s>/)) {
            $output .= " <s> ";
          }
          else {
            $output .= "     ";  
          }
        }
        ########################################################################
        # vytisknuti praveho kontextu
        ########################################################################
        my $gap2 = 1; # boolean, TRUE(1) ma se tisknout mezera
        my $j = $i+1; # aktualni prvek je potencialni konec vety, do $rcontextu zaradim az dalsi
        my $pom;
        my $rcontext = "";      
      
        while (length($rcontext) < $r) {
          if ($j < $#odstavec) {
            $pom = $odstavec[$j];
            if ($pom =~ /^<(f|f .*|d)>/) { 
              $pom =~ s/<.*>//;
              if ($gap2 == 1) {
                $rcontext .= " $pom";
              }
              else {
                $rcontext .= "$pom";
                $gap2 = 1;
              } 
            }
            elsif ($pom =~ /^<D>/) { $gap2 = 0; }      
          }
          else {
            last;
          }
          $j++;
        }
        $rcontext = substr($rcontext, 0, $r);
        $output .= "$rcontext\n";  
        ##### konec praveho kontextu
        if (not $temp =~ /<s>/) {
          if ($gap == 1) { # pridani znamenka do leveho kontextu, s mezerou ci bez
            $lcontext .= " $temp";
            $lcontext = substr($lcontext, $l);
          }
          else {
            $lcontext .= "$temp";
            $lcontext = substr($lcontext, $l);
            $gap = 1;
          }
        }               
      }
      elsif ($temp =~ /^<(f|f .*)>/) { # jedna se o slovo 
        $temp =~ s/<.*>//;
        if ($gap == 1) {
          $lcontext .= " $temp";
          $lcontext = substr($lcontext, $l);
        }
        else {
          $lcontext .= "$temp";
          $lcontext = substr($lcontext, $l);
          $gap = 1;
        } 
      }
      elsif ($temp =~ /<d>/) { # zpracovani znamenka
        $temp =~ s/<.*>//;
        if ($gap == 1) {
          $lcontext .= " $temp";
          $lcontext = substr($lcontext, $l);
        }
        else {
          $lcontext .= "$temp";
          $lcontext = substr($lcontext, $l);
          $gap = 1;
        }     
      }
      elsif ($temp =~ /^<D>/) { $gap = 0; } # zpracovani "nemezery"
    }       
  }
  return $output;
}

1;
