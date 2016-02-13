#!/usr/bin/perl

use strict;
use warnings;
use utf8;

use Getopt::Std;
our ($opt_E, $opt_C, $opt_L, $opt_M, $opt_T, $opt_S, $opt_K, $opt_f, $opt_s, $opt_b, 
     $opt_i, $opt_o, $opt_c, $opt_l, $opt_m, $opt_r, $opt_p, $opt_k, $opt_h, $opt_t);

use Encode;
use Victor::Cz::EncoDetect;
use Victor::Cz::LangID1;
use Victor::Cz::LangID2;
use Victor::Cz::Tokenizer;
use Victor::Cz::Segmenter;
use Victor::Cz::Concordance;
use Victor::Cz::Cleaner;

getopts('ECLMTSKf:sbi:o:c:l:m:p:r:k:ht');

sub help {
    print <<HELP
Text Segmentation. 
Typically used to text processing of text files in Czech language.

Usage
-----

* print help message:

textseg.pl -h

* for text processing: 

textseg.pl [-E] [-C] [-L] [-M] [-T] [-S] [-K] [-b] [-f "file"] [-i "encoding"] 
           [-o "encoding"] [-c N] [-l N] [-m N] [-p "text"] [-t] [-s] [-k N] 
           [input_file] [output_file] [concor_file]

If none of -ECLMTSK option is given then call the function with options -ECLTS.

        -E    Indicates that recognition of encoding will be done. If it is 
              only one uppercased option and option -b omitted, write 
              recognised encoding on STDIN.
        -C    Indicates that cleaning of text will be done.
        -L    Indicates that identification of language (best n-gram method) 
              will be done. If it is only one uppercased option and option -b 
              omitted, write conclusion on STDIN.
        -M    Indicates that identification of language (conditional trigram 
              method) will be done. If it is only one uppercased option and 
              option -b omitted, write conclusion on STDIN.              
        -T    Indicates that tokenization will be done.
        -S    Indicates that segmentation will be done. Don't use this option
              without using option -T with exceptions -T (or -ET) where 
              input_file had been tokenized before.
        -K    Indicates that concordance will be done. Don't use this option
              without using option -S with exceptions -K (or -EK) where 
              input_file had been segmented before.
        -b    Batch processing. Indicates that input_file contains paths to
              files which have to be processed (one path each line). If 
              output_file given it indicates path to directory where processed
              files will be saved. Correspondingly with concor_file.
        -f    "file" is path to log file. Implicit value is "info.log".
        -i    "encoding" may be "utf8" or "iso-8859-2" or "cp1250". Encoding of
              input file. If omitted then will be automatic recognize.
        -o    "encoding" may be "utf8" or "iso-8859-2" or "cp1250". Encoding of
              output file. If omitted then will have -i value.
        -c N  Determine how many words have to be in one line not to be 
              cleaned out. Implicit value is 10.              
        -l N  Determine treshold of language identification for -L variant. 
              (N from <0; 1>). Implicit value is 0.4, smaller number more 
              probable that text is Czech.
        -m N  Determine treshold of czech word for language recognition -M 
              variant (N from <1; 5>). Implicit value is 2.1, smaller number 
              more probable that text is Czech.
        -r N  Determine Czech words to all words ratio for language recognition
              -M variant (N from <0; 1>). Implicit value is 0.45, smaller 
              number more probable that text is Czech.      
        -p    "text" indicates start of new paragraph. Implicit value is empty 
              line i.e. "\\n\\n".
        -t    Indicates that special token <enter> will not be use in 
              tokenization.
        -s    Indicates that segmented text will be save in plaintext format.
              More about this format you can see in readme.txt file.
        -k N  N is count of characters in left and right context in 
              concordance. Implicit value is 50.              

 [input_file] Path to input file. If omitted then program will read from STDIN.
[output_file] If omitted then output will be written to STDOUT.
[concor_file] When both -S and -K are switched on segmented text will be saved 
              to output_file and concordanced text to concor_file. If omitted 
              then concordanced text will be saved as output_file with 
              extension ".con". If output_file is also omitted then text will 
              be saved as input_file with extension ".con". 
                 
HELP
}

sub implicit {
  $opt_E = 1;
  $opt_C = 1;
  $opt_L = 1;
  $opt_T = 1;
  $opt_S = 1;
}

if ($opt_h) { help(); exit 0; };

# defaults:
$opt_E || $opt_C || $opt_L || $opt_M || $opt_T || $opt_S || $opt_K || implicit();
$opt_f ||= "info.log";
$opt_s ||= 0;
$opt_b ||= 0;
$opt_E ||= 0;
$opt_C ||= 0;
$opt_L ||= 0;
$opt_M ||= 0;
$opt_T ||= 0;
$opt_S ||= 0;
$opt_K ||= 0;
$opt_l ||= 0.45;
$opt_m ||= 2.1; $opt_m *= -1;
$opt_r ||= 0.45;
$opt_p ||= "\n\n";
$opt_k ||= 50; 
$opt_t ||= 0;
$opt_c ||= 10;
my ($r_i, $r_o) = (1, 1);
if (defined $opt_i) {
  ($opt_i eq "utf8") or ($opt_i eq "iso-8859-2") or ($opt_i eq "cp1250") or 
    die "Wrong option value -i $opt_i.\n";
  $r_i = $opt_i;
}
if (defined $opt_o) {
  ($opt_o eq "utf8") or ($opt_o eq "iso-8859-2") or ($opt_o eq "cp1250") or 
    die "Wrong option value -o $opt_o.\n";
  $r_o = $opt_o;
}

my @files;
my $stdin = 0; # boolean 0 = nectu ze STDIN

open(INFO, ">$opt_f") || die "Can't create $opt_f logfile.\n";
print INFO encode("utf8", "file\t"); # header info souboru
if($opt_E || not defined $opt_i) { print INFO encode("utf8", "encoding\t"); }
if($opt_C) { print INFO encode("utf8", "cleaned/original\t"); }
if($opt_L) { print INFO encode("utf8", "language L method\t"); }
if($opt_M) { print INFO encode("utf8", "language M method\t"); }
if($opt_T) { print INFO encode("utf8", "tokens\tparagraphs\t"); }
if($opt_S) { print INFO encode("utf8", "sentences\t"); }
print INFO encode("utf8", "\n");
close(INFO);

my $error = ""; # vypisy chyb

if ($opt_b) {
  if (defined $ARGV[0]) {
    open(FILES, $ARGV[0]) or die "Error in opening file $ARGV[0]\n";
  	local $/; # takze to neovlivni $/ jinde
  	undef $/;  
    $_ = <FILES>;
    close (FILES);
  }
  else {
  	local $/; # takze to neovlivni $/ jinde
  	undef $/;
  	$_ = <>;   
  }
  @files = split(/\n/);
}
elsif (defined $ARGV[0]) { 
  $files[0] = $ARGV[0];
}
else {
	local $/; # takze to neovlivni $/ jinde
	undef $/;
	$_ = <>;
  $files[0] = "STDIN";
  $stdin = 1;  
}
@files || die "Empty input.\n";

for (my $i = 0; $i <= $#files; $i++) {
  open(INFO, ">>$opt_f") || die "Can't create $opt_f logfile.\n";
  my $info = ""; # informace o zpracovavanem souboru
  if (not defined $r_i) { $opt_i = undef; } # nulovani optionu, pokud nebyl zadan uzivatelem
  if (not defined $r_o) { $opt_o = undef; } # nulovani optionu, pokud nebyl zadan uzivatelem

  if ($stdin == 0) {
    if (not open(INPUT, $files[$i])) { 
      $error .= "Error in opening file $files[$i]\n"; 
      next; 
    }
    local $/; # takze to neovlivni $/ jinde
    undef $/;
    $_ = <INPUT>;
    close (INPUT);
  }
  $info .= "$files[$i]\t";
  if ($opt_E || not defined $opt_i) {
    $opt_i ||= detects($_);

    $info .= "$opt_i\t";
    if ($opt_E) {
      if (not($opt_b || $opt_C || $opt_L || $opt_M || $opt_T || $opt_S)) {
        last;
      }
    }
  }
  $opt_o ||= $opt_i;
  $_ = decode($opt_i, $_);
  
  if ($opt_C) {
    my $lorig = length($_);
    $_ = cleans($_, $opt_c);
    my $lclean = length($_);
    $info .= "$lclean/$lorig\t";
  }
  
  if ($opt_L) {
    my $a = $_;
    my $pvalue = recognize1($_);

    $info .= "$pvalue\t";
    my $temp = ($pvalue > $opt_l) ? "not czech " : "is czech ";
    if (not($opt_b || $opt_E || $opt_C || $opt_M || $opt_T || $opt_S)) { 
      print $temp . "P-value: $pvalue\n";
      last;
    }    
    if ($pvalue > $opt_l) { 
      $info .= "not czech\n";  
      print INFO encode("utf8", $info); close(INFO);
      next;       
    }
    $_ = $a;  
  }
  
  if ($opt_M) {
    my $a = $_;
    my $value = recognize2($_, $opt_m);

    $info .= "$value\t";
    my $temp = ($value > $opt_r) ? "not czech " : "is czech ";
    if (not($opt_b || $opt_E || $opt_C || $opt_L || $opt_T || $opt_S)) { 
      print $temp . "Value: $value\n";
      last;
    }    
    if ($value > $opt_r) { 
      $info .= "not czech\n";  
      print INFO encode("utf8", $info); close(INFO);
      next;       
    }
    $_ = $a;  
  }  
  
  if ($opt_T) {
    my ($t, $p);
    ($_, $t, $p) = tokens($_, $opt_p, $opt_t);

    $info .= "$t\t$p\t";
  }
  
  if ($opt_S) {
    my $s;
    ($_, $s) = segments($_, $opt_s);

    $info .= "$s\t";
  }
  
  $files[$i] =~ s/.*(\\|\/)//; # odstraneni cesty k souboru
  
  if ($opt_K) {
    if ($opt_S == 0) {
      $_ = concos($_, $opt_k, $opt_k);
    }
    else {
      my $con = concos($_, $opt_k, $opt_k);
      if ($opt_b) {
        if (defined $ARGV[2]) {
          if (not opendir (DIR, $ARGV[2])) { mkdir ($ARGV[2]); }
          else { closedir (DIR); }
          if (not open (OUTPUT, ">$ARGV[2]/$files[$i]")) {  # na linuxu nutno zmenit lomitko
            $error .= "Error in opening file $ARGV[2]/$files[$i].\n"; 
            $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
            next;             
          }
          print OUTPUT encode ($opt_o, $con);
          close (OUTPUT);
        }
        elsif (defined $ARGV[1]) {
          if (not opendir (DIR, $ARGV[1])) { mkdir ($ARGV[1]); }
          else { closedir (DIR); }
          if (not open (OUTPUT, ">$ARGV[1]/$files[$i].con")) {  # na linuxu nutno zmenit lomitko
            $error .= "Error in opening file $ARGV[1]/$files[$i].con.\n"; 
            $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
            next;             
          }
          print OUTPUT encode ($opt_o, $con);
          close (OUTPUT);        
        }
        else {
          if (not open (OUTPUT, ">$files[$i].con")) {
            $error .= "Error in opening file $files[$i].con.\n"; 
            $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
            next;             
          }
          print OUTPUT encode ($opt_o, $con);
          close (OUTPUT);        
        }        
      }
      elsif (defined $ARGV[2]) {
        if (not open (OUTPUT, ">$ARGV[2]")) {
          $error .= "Error in opening file $ARGV[2].\n"; 
          $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
          next;             
        }
        print OUTPUT encode ($opt_o, $con);
        close (OUTPUT);
      }
      elsif (defined $ARGV[1]) {
        if (not open (OUTPUT, ">$ARGV[1].con")) {
          $error .= "Error in opening file $ARGV[1].con.\n"; 
          $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
          next;             
        }
        print OUTPUT encode ($opt_o, $con);
        close (OUTPUT);      
      }
      else {
        if (not open (OUTPUT, ">$ARGV[0].con")) {
          $error .= "Error in opening file $ARGV[0].con.\n"; 
          $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
          next;             
        }
        print OUTPUT encode ($opt_o, $con);
        close (OUTPUT);      
      }
    }
  }
  
  if ($opt_b) {
    if (defined $ARGV[1]) {
      if (not opendir (DIR, $ARGV[1])) { mkdir ($ARGV[1]); }
      else { closedir (DIR); }
      if (not open (OUTPUT, ">$ARGV[1]/$files[$i]")) {  # na linuxu nutno zmenit lomitko
        $error .= "Error in opening file $ARGV[1]/$files[$i].\n"; 
        $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
        next;             
      }
      print OUTPUT encode ($opt_o, $_);
      close (OUTPUT);        
    }
    else {
      if (not open (OUTPUT, ">$files[$i].final")) {
        $error .= "Error in opening file $files[$i].final.\n"; 
        $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
        next;             
      }
      print OUTPUT encode ($opt_o, $_);
      close (OUTPUT);        
    }        
  }
  elsif (defined $ARGV[1]) {
    if (not open (OUTPUT, ">$ARGV[1]")) {
      $error .= "Error in opening file $ARGV[1].\n"; 
      $info .= "\n"; print INFO encode("utf8", $info); close(INFO);
      next;             
    }
    print OUTPUT encode ($opt_o, $_);
    close (OUTPUT);      
  }
  else {
    print encode ($opt_o, $_);      
  }
  $info .= "\n";  
  print INFO encode("utf8", $info);
  close(INFO);
}
    
print $error;
