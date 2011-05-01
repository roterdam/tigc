package scanner;
import java_cup.runtime.*;
import parser.sym;

%%

%class Scanner
%unicode
%cup
%line
%column
%public

%{
    StringBuffer string = new StringBuffer();
    int comment_level;

    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

Identifier = [A-Za-z] [A-Za-z_0-9]*

IntegerConstant = [0-9]+

%state STRING, COMMENT

%eofval{
    if (yystate() == STRING) {
        return symbol(sym.error, "Open string constant detected when EOF encountered");
    } else if (yystate() == COMMENT) {
        return symbol(sym.error, "Unclosed comment detected when EOF encountered");
    }
    return symbol(sym.EOF);
%eofval}

%%

/* keywords */
<YYINITIAL> "array"         { return symbol(sym.ARRAY); }
<YYINITIAL> "break"         { return symbol(sym.BREAK); }
<YYINITIAL> "do"            { return symbol(sym.DO); }
<YYINITIAL> "else"          { return symbol(sym.ELSE); }
<YYINITIAL> "end"           { return symbol(sym.END); }
<YYINITIAL> "for"           { return symbol(sym.FOR); }
<YYINITIAL> "function"      { return symbol(sym.FUNCTION); }
<YYINITIAL> "if"            { return symbol(sym.IF); }
<YYINITIAL> "in"            { return symbol(sym.IN); }
<YYINITIAL> "let"           { return symbol(sym.LET); }
<YYINITIAL> "nil"           { return symbol(sym.NIL); }
<YYINITIAL> "of"            { return symbol(sym.OF); }
<YYINITIAL> "then"          { return symbol(sym.THEN); }
<YYINITIAL> "to"            { return symbol(sym.TO); }
<YYINITIAL> "type"          { return symbol(sym.TYPE); }
<YYINITIAL> "var"           { return symbol(sym.VAR); }
<YYINITIAL> "while"         { return symbol(sym.WHILE); }

<YYINITIAL> {
    /* identifiers */
    {Identifier}            { return symbol(sym.IDENTIFIER, yytext()); }

    /* literals */
    {IntegerConstant}       {
                                Integer i = null;
                                try {
                                    i = new Integer(yytext());
                                    return symbol(sym.INTEGER_CONSTANT, i);
                                } catch (NumberFormatException e) {
                                    return symbol(sym.error, "Integer constant too big");
                                }
                            }
    \"                      { string.setLength(0); yybegin(STRING); }

    /* punctuation symbols */
    ","                     { return symbol(sym.COMMA); }
    ":"                     { return symbol(sym.COLON); }
    ";"                     { return symbol(sym.SEMICOLON); }
    "("                     { return symbol(sym.LEFT_PARENTHESIS); }
    ")"                     { return symbol(sym.RIGHT_PARENTHESIS); }
    "["                     { return symbol(sym.LEFT_SQUARE_BRACKET); }
    "]"                     { return symbol(sym.RIGHT_SQUARE_BRACKET); }
    "{"                     { return symbol(sym.LEFT_CURLY_BRACKET); }
    "}"                     { return symbol(sym.RIGHT_CURLY_BRACKET); }
    "."                     { return symbol(sym.DOT); }
    "+"                     { return symbol(sym.PLUS); }
    "-"                     { return symbol(sym.MINUS); }
    "*"                     { return symbol(sym.ASTERISK); }
    "/"                     { return symbol(sym.SLASH); }
    "="                     { return symbol(sym.EQ); }
    "<>"                    { return symbol(sym.NEQ); }
    "<"                     { return symbol(sym.LT); }
    "<="                    { return symbol(sym.LEQ); }
    ">"                     { return symbol(sym.GT); }
    ">="                    { return symbol(sym.GEQ); }
    "&"                     { return symbol(sym.AMPERSAND); }
    "|"                     { return symbol(sym.VERTICAL_BAR); }
    ":="                    { return symbol(sym.ASSIGNMENT); }

    /* whitespace */
    {WhiteSpace}            { /* ignore */ }

    /* comment */
    "/*"                    { comment_level = 1; yybegin(COMMENT); }
}

<STRING> {
    \"                      { yybegin(YYINITIAL); return symbol(sym.STRING_CONSTANT, string.toString()); }
    \\n                     { string.append("\n"); }
    \\t                     { string.append("\t"); }
    \\\"                    { string.append("\""); }
    \\\\                    { string.append("\\"); }
    \\[0-9]{3}              {
                                int i;
                                try {
                                    i = Integer.parseInt(yytext().substring(1));
                                } catch (NumberFormatException e) {
                                    return symbol(sym.error, yytext() + " is not a number");
                                }
                                if (i < 0 || i >= 128)
                                    return symbol(sym.error, yytext() + " is not a valid ascii code");
                                char c = (char)i;
                                string.append(c);
                            }
    \\\^[@A-Z\[\\\]\^_]     {
                                char c = yytext().charAt(2);
                                c -= 64;
                                string.append(c);
                            }
    \\[ \t\n\r\f]+\\        { /* do nothing */ }
    [\n\r\f\t]              { return symbol(sym.error, "Newlines, returns, tabs or formfeeds are not allowered here"); }
    [^\t\n\r\f\"\\]+        { string.append( yytext() ); }
}

<COMMENT> {
    "/*"                    { ++comment_level; }
    "*/"                    { --comment_level;
                              if (comment_level == 0) yybegin(YYINITIAL); }
    [^]                     { /* ignore */ }
}

/* error fallback */
.|\n                        { return symbol(sym.error, "Illegal character \"" + yytext() + "\""); }

