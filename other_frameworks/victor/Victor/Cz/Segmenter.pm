#!/usr/bin/perl

package Victor::Cz::Segmenter;
use Exporter;
@ISA = qw (Exporter);
@EXPORT = qw (&segments);
################################################################################
# importovane moduly a pouzita pragmata:
# utf8 - pragma; zdrojovy kod je napsan v tomto kodovani
# Encode - modul; prevody mezi kodovanimi 
################################################################################
use strict;
use warnings;
use utf8;
use Encode;

################################################################################
# inicializace, nacteni zkratek do globalnich promennych
################################################################################
my $datadir = $INC{"Victor/Cz/Segmenter.pm"} || "./Segmenter.pm";
$datadir =~ s/Segmenter\.pm$//;

my (%abbr_12, %abbr_12_short, %abbr_13, %abbr_14);

open(ABBR_12, "$datadir/abbr_not_s.dat") or die "Can't find file abbr_not_s.dat with list of abbreviations.\n";
while(<ABBR_12>) {
  local $_ = decode("utf8", $_);
  chomp;
  $abbr_12{$_} = 1;
}
close ABBR_12;

open(ABBR_12_SHORT, "$datadir/abbr_negative_2.dat") or die "Can't find file abbr_negative_2.dat with list of abbreviations.\n";
while(<ABBR_12_SHORT>) {
  local $_ = decode("utf8", $_);
  chomp;
  $abbr_12_short{$_} = 1;
}
close ABBR_12_SHORT;

open(ABBR_13, "$datadir/abbr_may_s.dat") or die "Can't find file abbr_may_s.dat with list of abbreviations.\n";
while(<ABBR_13>) {
  local $_ = decode("utf8", $_);
  chomp;
  $abbr_13{$_} = 1;
}
close ABBR_13;

open(ABBR_14, "$datadir/abbr=apel.dat") or die "Can't find file abbr=apel.dat with list of abbreviations.\n";
while(<ABBR_14>) {
  local $_ = decode("utf8", $_);
  chomp;
  $abbr_14{$_} = 1;
}
close ABBR_14;

open(QUOTES, "$datadir/quotes.dat") or die "Can't find file quotes.dat with list of quotation marks and right (closing) brackets.\n";
local $_ = <QUOTES>; $_ = decode ("utf8", $_); chomp;
my $quotes = $_;
close (QUOTES);

my $regexp;
################################################################################
# rozhodovaci strom
################################################################################
sub decision_tree (@) {
  my (@pole) = @_;
  my $string = "";
  if ($pole[16] == 0) {
    if ($pole[3] == 0) {
      if ($pole[11] == 0) {
        if ($pole[8] == 0) {
          if ($pole[12] == 0) {
            if ($pole[13] == 0) {
              if ($pole[14] == 0) { $string = "</s>\n<s>\n"; }
              else {
                if ($pole[0] == 0) { $string = "</s>\n<s>\n"; }
                else { ; }
              }
            }
            else {
              if ($pole[6] == 0) { ; }
              else { $string = "</s>\n<s>\n"; }
            }
          }
          else {
            if ($pole[0] == 0) { $string = "</s>\n<s>\n"; }
            else { ; }
          }
        }
        else { ; }
      }
      else {
        if ($pole[6] == 0) {
          if ($pole[0] == 0) { $string = "</s>\n<s>\n"; }
          else {
            if ($pole[7] == 0) {
              if ($pole[9] == 0) { ; }
              else {
                if ($pole[8] == 0) { $string = "</s>\n<s>\n"; }
                else { ; }
              }
            }
            else { ; }
          }
        }
        else {
          if ($pole[0] == 0) { $string = "</s>\n<s>\n"; }
          else {
            if ($pole[10] == 0) { $string = "</s>\n<s>\n"; }
            else { ; }
          }
        }      
      }
    }
    else {
      if ($pole[15] == 0) { ;}
      else {
        if ($pole[9] == 0) {
          if ($pole[8] == 0) {
            if ($pole[6] == 0) {
              if ($pole[10] == 0) {
                if ($pole[5] == 0) { ; }
                else { $string = "</s>\n<s>\n"; }
              }
              else {
                if ($pole[12] == 0) { ; }
                else { $string = "</s>\n<s>\n"; }
              }
            }
            else {
              if ($pole[15] == 0) { ; }
              else { $string = "</s>\n<s>\n"; }
            }
          }
          else { $string = "</s>\n<s>\n"; }
        }
        else { ; }
      }
    }
  }
  else {
    if ($pole[6] == 0) {
      if ($pole[3] == 0) { ; }
      else {
        if ($pole[8] == 0) { $string = "</s>\n<s>\n"; }
        else { ; }
      }
    }
    else { ; }
  }
  return $string;
}

################################################################################
# exportovana procedura segments (inputfile, outputfile, - povinne
#                                 input kodovani, output kodovani - volitelne,
#                                 implicitni hodnota = "utf8")
# zpracuje text pripraveny procedurou tokens, prida zacatky vet
################################################################################
sub segments {
  ##############################################################################
  # inicializace - zpracovani parametru procedury, otevreni souboru
  ##############################################################################
  my ($input, $opt_a) = @_;
  (length($input)) or die "Write at least one argument input text.\n";
  $opt_a ||= 0;
  
  my $n = 0; # pocitadlo odstavcu
  my $s = 0; # pocitadlo vet
  my $output = "";
  
  ##############################################################################
  # hlavni program
  ##############################################################################
  my $index = index($input, "<c>\n");
  if ($index >= 0) { # obsahuje hlavicku, tak ji dam do outputu, pokud ji nema, predpokladam, ze zacina hned tokenem <p>
    if ($opt_a == 0) { $output .= substr($input, 0, $index + 4); }  
    $input = substr($input, $index + 4);
  }
  
  my @input = split(/<\/p>\n/, $input);
  for (my $k = 0; $k < $#input; $k++) {   
    local $_ = $input[$k]; # nacteni a prekodovani odstavce do utf8
    my @odstavec = split(/\n/);
    if ($odstavec[$#odstavec] eq "<enter>") { # TODO neohrabane reseni, aby to nepsalo <s> po <d>. nasledovane <enter> na konci odstavce
      $#odstavec--;
    }
    for(my $i = 0; $i < $#odstavec; $i++) { # chybi "=", protoze za poslednim prvkem nemuze byt token <s>, 
                                            # po skonceni cyklu posledni prvek vypisu do OUTPUT souboru
      $regexp = "<d>[.!?" . $quotes . "]";
      if ( ($odstavec[$i] =~ /$regexp/) or ($odstavec[$i] eq "<enter>") ) { # mozne konce vet
        my @pole;
        $pole[0] = ($odstavec[$i] eq "<d>.") ? 1 : 0; #  0.  aktualni token je tecka
        $pole[1] = ($odstavec[$i] eq "<d>!") ? 1 : 0; #  1.  aktualni token je vykricnik
        $pole[2] = ($odstavec[$i] eq "<d>?") ? 1 : 0; #  2.  aktualni token je otaznik
        $regexp = "<d>[" . $quotes . "]";
        $pole[3] = ($odstavec[$i] =~ /$regexp/) ? 1 : 0; #  3.  aktualni token je uvozovka nebo zavorka
        $pole[4] = ($odstavec[$i] eq "<enter>") ? 1 : 0; #  4.  aktualni token je <enter>
  
        if ($i == $#odstavec) {
           $pole[5] = 1; #  5.  dalsi token neni (aktualni token je posledni)
           $pole[6] = 0; #  6.  dalsi token je <f cap>
           $pole[7] = 0; #  7.  dalsi token je <f upper> nebo <f mixed>
           $pole[8] = 0; #  8.  dalsi token je <D>
           $pole[9] = 0; #  9.  2. dalsi token je <d>[.,!?)"]
          $pole[10] = 0; # 10.  2. dalsi token je <f num> 
        }
        else {
          $pole[5] = 0; #  5.  dalsi token neni (aktualni token je posledni)
          $pole[6] = ($odstavec[$i+1] =~ /<f cap>/) ? 1 : 0; #  6.  dalsi token je <f cap>
          $pole[7] = ($odstavec[$i+1] =~ /<f (upper|mixed)>/) ? 1 : 0; #  7.  dalsi token je <f upper> nebo <f mixed>
          $pole[8] = ($odstavec[$i+1] eq "<D>") ? 1 : 0; #  8.  dalsi token je <D>        
          if ($i+1 == $#odstavec) {
             $pole[9] = 0;
            $pole[10] = 0;
          }
          else {
            $regexp = "<d>[,.!?" . $quotes . "]";
             $pole[9] = ($odstavec[$i+2] =~ /$regexp/) ? 1 : 0; #  9.  2. dalsi token je <d>[.,!?)"]
            $pole[10] = ($odstavec[$i+2] =~ /<f num>/) ? 1 : 0; # 10.  2. dalsi token je <f num>
          }           
        }
        
        if ($i <= 1) {
          $pole[11] = 0; # 11.  predchozi token je <f num>
          $pole[12] = 0; # 12.  predchozi token je <f abbr>, ktery urcite neoznacuje konec vety
          $pole[13] = 0; # 13.  predchozi token je <f abbr>, ktery muze koncit vetu
          $pole[14] = 0; # 14.  predchozi token je <f abbr>, ktera vsak je rovna apelativu
          if ($i == 0) {
            $pole[15] = 0; # 15. predchozi token je <D>
            $pole[16] = 0; # 16.  dalsi token je <d>[.,!?)"]
          }
          else {
            $pole[15] = ($odstavec[0] eq "<D>") ? 1 : 0; # 15. predchozi token je <D>
            $regexp = "<d>[.!?" . $quotes . "]";
            $pole[16] = ($odstavec[0] =~ /$regexp/) ? 1 : 0; # 16.  dalsi token je <d>[.!?)"]
          }
        }
        else {
          $pole[11] = ($odstavec[$i-2] =~ /<f num>/) ? 1 : 0; # 11.  2. predchozi token je <f num>
          $pole[15] = ($odstavec[$i-1] eq "<D>") ? 1 : 0; # 15. predchozi token je <D>
          $regexp = "<d>[.!?" . $quotes . "]";
          $pole[16] = ($odstavec[$i-1] =~ /$regexp/) ? 1 : 0; # 16.  dalsi token je <d>[.!?)"]
          if ($odstavec[$i-2] =~ /<f(| upper| mixed| cap)>/) {
            my $pom = $odstavec[$i-2];
            $pom =~ s/<.*>//;
            my $pom_lc = $pom;
            $pom_lc =~ tr/A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ/a-záčďéěíňóřšťúůýž/;
            if (length($pom) > 2) {
              $pole[12] = (exists $abbr_12{$pom_lc}) ? 1 : 0; # 12.  2. predchozi token je <f abbr>, ktery urcite neoznacuje konec vety
            }
            else {
              $pole[12] = (exists $abbr_12_short{$pom}) ? 0 : 1; # 12.  negace zkratek pri kratsich slovech
            }
            $pole[13] = (exists $abbr_13{$pom}) ? 1 : 0; # 13.  2. predchozi token je <f abbr>, ktery muze koncit vetu
            $pole[14] = (exists $abbr_14{$pom}) ? 1 : 0; # 14.  2. predchozi token je <f abbr>, ktera vsak je rovna apelativu
          }
          else {
            $pole[12] = 0; # 12.  2. predchozi token je <f abbr>, ktery urcite neoznacuje konec vety
            $pole[13] = 0; # 13.  2. predchozi token je <f abbr>, ktery muze koncit vetu
            $pole[14] = 0; # 14.  2. predchozi token je <f abbr>, ktera vsak je rovna apelativu          
          }
        } 
        if ($odstavec[$i] ne "<enter>") { # token <enter> je pomocny, do vystupu ho nepisu
          $output .= "$odstavec[$i]\n";
        }
        my $decision = decision_tree(@pole); # zapsani si nezapsani zacatku vety 
        if ($decision ne "") { $s++; }
        $output .= $decision;             
      }
      else { # nejde o token, za kterym by mohl byt konec vety             
        $output .= "$odstavec[$i]\n";        
          
        if ($odstavec[$i] =~ /^<p n=.*>$/) { # za kazdym zacatkem odstavce zacina veta
          $output .= "<s>\n";
          $s++;
        }
      }
    }
    if ($odstavec[$#odstavec] ne "<enter>") { # po skonceni cyklu vypsani posledniho prvku v odstavci, 
                                              # za kterym nemuze byt zacatek vety
      $output .= "$odstavec[$#odstavec]\n"; 
    }
    $output .= "</s>\n</p>\n";
  }
  
  if ($opt_a) { # ulozeni vystupu v plaintextu
    my $line = "";
    chomp($output);
    my @paragraphs = split(/<\/p>/, $output);
    for (my $i = 0; $i <= $#paragraphs; $i++) {
      $paragraphs[$i] =~ s/[^\n]*\n//; # odstraneni prvniho radku s tokenem <p>
      chomp($paragraphs[$i]);
      my @sentences = split(/<\/s>/, $paragraphs[$i]);
      for (my $j = 0; $j <= $#sentences; $j++) {
        $sentences[$j] =~ s/[^\n]*\n//; # odstraneni prvniho radku s tokenem <s>
        chomp($sentences[$j]);
        my @tokens = split(/\n/, $sentences[$j]);
        my $gap = 0;
        for (my $k = 0; $k <= $#tokens; $k++) {
          if ($tokens[$k] =~ /^<(f|f .*|d)>/) { 
            $tokens[$k] =~ s/<.*>//;
            if ($gap == 1) {
              $line .= " $tokens[$k]";
            }
            else {
              $line .= "$tokens[$k]";
              $gap = 1;
            } 
          }
          elsif ($tokens[$k] =~ /^<D>/) { $gap = 0; }                
        }
        $line .= "\n"; # konec vety        
      }
      if ($i < $#paragraphs) {$line .= "\n"; } # konec odstavce
    }    
    return ($line, $s); 
  }  
  else { # upraveni formatu zkratek, tecka se soucasti tokenu
    my @odstavec = split(/\n/, $output);
    $output = ""; # vynulovani vystupu
    
    for (my $i = 0; $i <= $#odstavec - 2; $i++) {
      if (($odstavec[$i+1] eq "<D>") and ($odstavec[$i+2] eq "<d>.")) {
        # podminka popisuje slovo nasledovane teckou, tj. potencialni zkratku
        my $abbr = $odstavec[$i];
        $abbr =~ s/<f(| cap| upper| mixed)>//;
        my $abbr_lc = $abbr;
        $abbr_lc =~ tr/A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ/a-záčďéěíňóřšťúůýž/;
        if (length($abbr) > 2) {
          if ((exists $abbr_12{$abbr_lc}) or (exists $abbr_13{$abbr})) { 
            # zkratka = apelativum je brana za zkratku (TODO)
            if ($odstavec[$i] =~ /<f cap>/) {
              $output .= "<f cap.abbr>$abbr.\n";
              $i += 2;
            }
            elsif ($odstavec[$i] =~ /<f upper>/) {
              $output .= "<f upper.abbr>$abbr.\n";
              $i += 2;
            }
            elsif ($odstavec[$i] =~ /<f mixed>/) {
              $output .= "<f mixed.abbr>$abbr.\n";
              $i += 2;
            }
            else{
              $output .= "<f abbr>$abbr.\n";
              $i += 2;
            }              
          }
          else {
            $output .= "$odstavec[$i]\n";
          }
        }
        else {
          if (not exists $abbr_12_short{$abbr}) {
            if ($odstavec[$i] =~ /<f cap>/) {
              $output .= "<f cap.abbr>$abbr.\n";
              $i += 2;
            }
            elsif ($odstavec[$i] =~ /<f upper>/) {
              $output .= "<f upper.abbr>$abbr.\n";
              $i += 2;
            }
            elsif ($odstavec[$i] =~ /<f mixed>/) {
              $output .= "<f mixed.abbr>$abbr.\n";
              $i += 2;
            }
            else{
              $output .= "<f abbr>$abbr.\n";
              $i += 2;
            }              
          }
          else {
            $output .= "$odstavec[$i]\n";
          }
        }
      }
      else {
        $output .= "$odstavec[$i]\n";
      }
    }    
    $output .= "$odstavec[$#odstavec-1]\n$odstavec[$#odstavec]\n"; # posledni 2 prvky
    $output =~ s/<s>\n<D>/<s>/g; # zvlastni pripad umisteni konce vet, kde chybi mezera za znakem, ktery ji oznacuje
    $output .= "</c>\n</doc>\n</csts>\n"; # patka output souboru
    return ($output, $s);
  }
return ($output, $s);
}

1;
