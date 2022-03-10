package com.matus.analyzers;

import com.matus.Main;

import java_cup.runtime.Symbol;

%%
%class LexicAnalyzer
%cupsym Symbols
%public
%line
%column
%char
%cup
%unicode
%ignorecase

delimiters = [ \r\t\n]+

comment = \/\/[^\r\n]*
multicomment = "<!"[^!>]*"!>"
word = [a-zA-Z]+
number = [0-9]+
string = \"[^\"]*\"

s_newline = \\n
s_simplequote = \\'
s_doublequote = \\\"
range = \~
//                                        space

underscore = _
range_special_char = [!#-$&-\)/=\?@\[-\^`]|\" \" | {underscore} //TODO this are missing language chars like   {} "space" dot | //TODO CANT USE < cause of multi comment

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
plus_sign = \+

//abby = ♥

id = {word}({word}|{number}|{underscore})*

section_separator = \%\%

%%
<YYINITIAL>{delimiters} {}


<YYINITIAL>{comment} { System.out.println("Reconocio token:<comment> lexema:"+yytext());Main.logToken("comment", yytext(), yyline, yycolumn); }
<YYINITIAL>{multicomment} { System.out.println("Reconocio token:<multicomment> lexema:"+yytext());Main.logToken("multicomment", yytext(), yyline, yycolumn);}

<YYINITIAL>{id} {System.out.println("Reconocio token:<id> lexema:"+yytext() + ":" + yyline + "-" + yycolumn);Main.logToken("id", yytext(), yyline, yycolumn);return new Symbol(Symbols.id, yycolumn, yyline, yytext());}
//<YYINITIAL>{word} {System.out.println("Reconocio token:<word> lexema:"+yytext());Main.logToken("comment", yytext(), yyline, yycolumn);return new Symbol(Symbols.word, yycolumn, yyline, yytext());}
<YYINITIAL>{number} {System.out.println("Reconocio token:<number> lexema:"+yytext());Main.logToken("number", yytext(), yyline, yycolumn);return new Symbol(Symbols.number, yycolumn, yyline, yytext());}
<YYINITIAL>{string} {System.out.println("Reconocio token:<string> lexema:"+yytext());Main.logToken("string", yytext(), yyline, yycolumn);return new Symbol(Symbols.string, yycolumn, yyline, yytext());}

<YYINITIAL>{s_newline} {System.out.println("Reconocio token:<s_newline> lexema:"+yytext());Main.logToken("s_newline", yytext(), yyline, yycolumn);return new Symbol(Symbols.s_newline, yycolumn, yyline, yytext());}
<YYINITIAL>{s_simplequote} {System.out.println("Reconocio token:<s_simplequote> lexema:"+yytext());Main.logToken("s_simplequote", yytext(), yyline, yycolumn);return new Symbol(Symbols.s_simplequote, yycolumn, yyline, yytext());}
<YYINITIAL>{s_doublequote} {System.out.println("Reconocio token:<s_doublequote> lexema:"+yytext());Main.logToken("s_doublequote", yytext(), yyline, yycolumn);return new Symbol(Symbols.s_doublequote, yycolumn, yyline, yytext());}
<YYINITIAL>{range} {System.out.println("Reconocio token:<range> lexema:"+yytext());Main.logToken("range", yytext(), yyline, yycolumn);return new Symbol(Symbols.range, yycolumn, yyline, yytext());}

<YYINITIAL>{range_special_char} {System.out.println("Reconocio token:<range_special_char> lexema:"+yytext());Main.logToken("range_special_char", yytext(), yyline, yycolumn);return new Symbol(Symbols.range_special_char, yycolumn, yyline, yytext());}

<YYINITIAL>{key_o} {System.out.println("Reconocio token:<key_o> lexema:"+yytext());Main.logToken("key_o", yytext(), yyline, yycolumn);return new Symbol(Symbols.key_o, yycolumn, yyline, yytext());}
<YYINITIAL>{key_c} {System.out.println("Reconocio token:<key_c> lexema:"+yytext());Main.logToken("key_c", yytext(), yyline, yycolumn);return new Symbol(Symbols.key_c, yycolumn, yyline, yytext());}
<YYINITIAL>{score} {System.out.println("Reconocio token:<score> lexema:"+yytext() + "-" + yyline + "-" + yycolumn);Main.logToken("score", yytext(), yyline, yycolumn);return new Symbol(Symbols.score, yycolumn, yyline, yytext());}
<YYINITIAL>{morethan} {System.out.println("Reconocio token:<morethan> lexema:"+yytext());Main.logToken("morethan", yytext(), yyline, yycolumn);return new Symbol(Symbols.morethan, yycolumn, yyline, yytext());}
<YYINITIAL>{colon} {System.out.println("Reconocio token:<colon> lexema:"+yytext());Main.logToken("colon", yytext(), yyline, yycolumn);return new Symbol(Symbols.colon, yycolumn, yyline, yytext());}
<YYINITIAL>{semicolon} {System.out.println("Reconocio token:<semicolon> lexema:"+yytext());Main.logToken("semicolon", yytext(), yyline, yycolumn);return new Symbol(Symbols.semicolon, yycolumn, yyline, yytext());}
<YYINITIAL>{comma} {System.out.println("Reconocio token:<comma> lexema:"+yytext());Main.logToken("comma", yytext(), yyline, yycolumn);return new Symbol(Symbols.comma, yycolumn, yyline, yytext());}
<YYINITIAL>{dot} {System.out.println("Reconocio token:<dot> lexema:"+yytext());Main.logToken("dot", yytext(), yyline, yycolumn);return new Symbol(Symbols.dot, yycolumn, yyline, yytext());}
<YYINITIAL>{asterisk} {System.out.println("Reconocio token:<asterisk> lexema:"+yytext());Main.logToken("asterisk", yytext(), yyline, yycolumn);return new Symbol(Symbols.asterisk, yycolumn, yyline, yytext());}
<YYINITIAL>{or_sign} {System.out.println("Reconocio token:<or_sign> lexema:"+yytext());Main.logToken("or_sign", yytext(), yyline, yycolumn);return new Symbol(Symbols.or_sign, yycolumn, yyline, yytext());}
<YYINITIAL>{plus_sign} {System.out.println("Reconocio token:<plus_sign> lexema:"+yytext());Main.logToken("plus_sign", yytext(), yyline, yycolumn);return new Symbol(Symbols.plus_sign, yycolumn, yyline, yytext());}

<YYINITIAL>{section_separator} {System.out.println("Reconocio token:<section_separator> lexema:"+yytext());Main.logToken("section_separator", yytext(), yyline, yycolumn);return new Symbol(Symbols.section_separator, yycolumn, yyline, yytext());}

<YYINITIAL>. {Main.logLexicError(yytext(),yyline, yycolumn);System.out.println("Error Lexico: "+yytext()+" Linea:"+yyline+" Columna:"+yycolumn);if (Main.stopLexOnError){throw new Error("Illegal character <"+yytext()+">");}}
