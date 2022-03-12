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

s_newline = \\n
s_simplequote = \\'
s_doublequote = \\\"
//string = \"[^┤\"]*\"
string = \"[^\"]*\"
//s_dq = \"┤\"

range = \~

underscore = _
question_mark = \?
range_special_char = [!#-$&-\)/=\@\[-\^`]|\" \"|{underscore} //TODO this are missing language chars like   {} "space" dot | //TODO CANT USE < cause of multi comment

key_o = \{
key_c = \}
score = -
morethan = >
lessthan = <
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


<YYINITIAL>{comment} {  Main.logToken("comment", yytext(), yyline, yycolumn); }
<YYINITIAL>{multicomment} {  Main.logToken("multicomment", yytext(), yyline, yycolumn);}

<YYINITIAL>{id} { Main.logToken("id", yytext(), yyline, yycolumn);return new Symbol(Symbols.id, yycolumn, yyline, yytext());}
//<YYINITIAL>{word} { Main.logToken("comment", yytext(), yyline, yycolumn);return new Symbol(Symbols.word, yycolumn, yyline, yytext());}
<YYINITIAL>{number} { Main.logToken("number", yytext(), yyline, yycolumn);return new Symbol(Symbols.number, yycolumn, yyline, yytext());}
<YYINITIAL>{string} { Main.logToken("string", yytext(), yyline, yycolumn);return new Symbol(Symbols.string, yycolumn, yyline, yytext());}

<YYINITIAL>{s_newline} { Main.logToken("s_newline", yytext(), yyline, yycolumn);return new Symbol(Symbols.s_newline, yycolumn, yyline, yytext());}
<YYINITIAL>{s_simplequote} { Main.logToken("s_simplequote", yytext(), yyline, yycolumn);return new Symbol(Symbols.s_simplequote, yycolumn, yyline, yytext());}
<YYINITIAL>{s_doublequote} { Main.logToken("s_doublequote", yytext(), yyline, yycolumn);return new Symbol(Symbols.s_doublequote, yycolumn, yyline, yytext());}
<YYINITIAL>{range} { Main.logToken("range", yytext(), yyline, yycolumn);return new Symbol(Symbols.range, yycolumn, yyline, yytext());}

<YYINITIAL>{range_special_char} { Main.logToken("range_special_char", yytext(), yyline, yycolumn);return new Symbol(Symbols.range_special_char, yycolumn, yyline, yytext());}
//<YYINITIAL>{s_dq} { Main.logToken("s_dq", yytext(), yyline, yycolumn);return new Symbol(Symbols.s_dq, yycolumn, yyline, yytext());}
<YYINITIAL>{question_mark} { Main.logToken("question_mark", yytext(), yyline, yycolumn);return new Symbol(Symbols.question_mark, yycolumn, yyline, yytext());}

<YYINITIAL>{key_o} { Main.logToken("key_o", yytext(), yyline, yycolumn);return new Symbol(Symbols.key_o, yycolumn, yyline, yytext());}
<YYINITIAL>{key_c} { Main.logToken("key_c", yytext(), yyline, yycolumn);return new Symbol(Symbols.key_c, yycolumn, yyline, yytext());}
<YYINITIAL>{score} { Main.logToken("score", yytext(), yyline, yycolumn);return new Symbol(Symbols.score, yycolumn, yyline, yytext());}
<YYINITIAL>{morethan} { Main.logToken("morethan", yytext(), yyline, yycolumn);return new Symbol(Symbols.morethan, yycolumn, yyline, yytext());}
<YYINITIAL>{lessthan} { Main.logToken("lessthan", yytext(), yyline, yycolumn);return new Symbol(Symbols.lessthan, yycolumn, yyline, yytext());}
<YYINITIAL>{colon} { Main.logToken("colon", yytext(), yyline, yycolumn);return new Symbol(Symbols.colon, yycolumn, yyline, yytext());}
<YYINITIAL>{semicolon} { Main.logToken("semicolon", yytext(), yyline, yycolumn);return new Symbol(Symbols.semicolon, yycolumn, yyline, yytext());}
<YYINITIAL>{comma} { Main.logToken("comma", yytext(), yyline, yycolumn);return new Symbol(Symbols.comma, yycolumn, yyline, yytext());}
<YYINITIAL>{dot} { Main.logToken("dot", yytext(), yyline, yycolumn);return new Symbol(Symbols.dot, yycolumn, yyline, yytext());}
<YYINITIAL>{asterisk} { Main.logToken("asterisk", yytext(), yyline, yycolumn);return new Symbol(Symbols.asterisk, yycolumn, yyline, yytext());}
<YYINITIAL>{or_sign} { Main.logToken("or_sign", yytext(), yyline, yycolumn);return new Symbol(Symbols.or_sign, yycolumn, yyline, yytext());}
<YYINITIAL>{plus_sign} { Main.logToken("plus_sign", yytext(), yyline, yycolumn);return new Symbol(Symbols.plus_sign, yycolumn, yyline, yytext());}

<YYINITIAL>{section_separator} { Main.logToken("section_separator", yytext(), yyline, yycolumn);return new Symbol(Symbols.section_separator, yycolumn, yyline, yytext());}

<YYINITIAL>. {Main.logLexicError(yytext(),yyline, yycolumn); if (Main.stopLexOnError){throw new Error("Illegal character <"+yytext()+">");}}
