package com.matus.analyzers;

import java_cup.runtime.Symbol;
import java_cup.runtime.*;

%%
%class LexicAnalyzer
%public
%cupsym Symbols
%line
%char
%cup
%unicode
%ignorecase

delimiters = [ \r\t\n]+
comment = \/\/[^\r\n]*
multicomment = <!.*!>
word = [a-zA-Z]+
number = [0-9]+
string = \"[^\"]\"

s_newline = \\n
s_simplequote = \\'
s_doublequote = \\\"
range = \~

//range_special_char = [!#-$&-+\.-/<-=\?@\[-`\|]|\" \" //TODO this are missing language chars like > - {} "space" //TODO remove dot, remove |


key_o = \{
key_c = \}
score = -
morethan = >
colon = :
semicolon = ;
comma = ,
dot = \.
asterisk = \*
or_sign = \|

id = {word}({word}*{number}*)*

section_separator = \%\%




%%
<YYINITIAL>{delimiters} {}
<YYINITIAL>{comment} {}
<YYINITIAL>{multicomment} {}

<YYINITIAL>{id} {System.out.println("Reconocio token:<id> lexema:"+yytext());return new Symbol(Symbols.id, yycolumn, yyline, yytext());}
//<YYINITIAL>{word} {System.out.println("Reconocio token:<word> lexema:"+yytext());return new Symbol(Symbols.word, yycolumn, yyline, yytext());}
<YYINITIAL>{number} {System.out.println("Reconocio token:<number> lexema:"+yytext());return new Symbol(Symbols.number, yycolumn, yyline, yytext());}
<YYINITIAL>{string} {System.out.println("Reconocio token:<string> lexema:"+yytext());return new Symbol(Symbols.string, yycolumn, yyline, yytext());}

<YYINITIAL>{s_newline} {System.out.println("Reconocio token:<s_newline> lexema:"+yytext());return new Symbol(Symbols.s_newline, yycolumn, yyline, yytext());}
<YYINITIAL>{s_simplequote} {System.out.println("Reconocio token:<s_simplequote> lexema:"+yytext());return new Symbol(Symbols.s_simplequote, yycolumn, yyline, yytext());}
<YYINITIAL>{s_doublequote} {System.out.println("Reconocio token:<s_doublequote> lexema:"+yytext());return new Symbol(Symbols.s_doublequote, yycolumn, yyline, yytext());}
<YYINITIAL>{range} {System.out.println("Reconocio token:<range> lexema:"+yytext());return new Symbol(Symbols.range, yycolumn, yyline, yytext());}

//<YYINITIAL>{range_special_char} {System.out.println("Reconocio token:<range_special_char> lexema:"+yytext());return new Symbol(Symbols.range_special_char, yycolumn, yyline, yytext());}

<YYINITIAL>{key_o} {System.out.println("Reconocio token:<key_o> lexema:"+yytext());return new Symbol(Symbols.key_o, yycolumn, yyline, yytext());}
<YYINITIAL>{key_c} {System.out.println("Reconocio token:<key_c> lexema:"+yytext());return new Symbol(Symbols.key_c, yycolumn, yyline, yytext());}
<YYINITIAL>{score} {System.out.println("Reconocio token:<score> lexema:"+yytext());return new Symbol(Symbols.score, yycolumn, yyline, yytext());}
<YYINITIAL>{morethan} {System.out.println("Reconocio token:<morethan> lexema:"+yytext());return new Symbol(Symbols.morethan, yycolumn, yyline, yytext());}
<YYINITIAL>{colon} {System.out.println("Reconocio token:<colon> lexema:"+yytext());return new Symbol(Symbols.colon, yycolumn, yyline, yytext());}
<YYINITIAL>{semicolon} {System.out.println("Reconocio token:<semicolon> lexema:"+yytext());return new Symbol(Symbols.semicolon, yycolumn, yyline, yytext());}
<YYINITIAL>{comma} {System.out.println("Reconocio token:<comma> lexema:"+yytext());return new Symbol(Symbols.comma, yycolumn, yyline, yytext());}
<YYINITIAL>{dot} {System.out.println("Reconocio token:<dot> lexema:"+yytext());return new Symbol(Symbols.dot, yycolumn, yyline, yytext());}
<YYINITIAL>{asterisk} {System.out.println("Reconocio token:<asterisk> lexema:"+yytext());return new Symbol(Symbols.asterisk, yycolumn, yyline, yytext());}
<YYINITIAL>{or_sign} {System.out.println("Reconocio token:<or_sign> lexema:"+yytext());return new Symbol(Symbols.or_sign, yycolumn, yyline, yytext());}

<YYINITIAL>{section_separator} {System.out.println("Reconocio token:<section_separator> lexema:"+yytext());return new Symbol(Symbols.section_separator, yycolumn, yyline, yytext());}


<YYINITIAL>. {System.out.println("Error Lexico : "+yytext()+"Linea"+yyline+" Columna "+yycolumn);}
