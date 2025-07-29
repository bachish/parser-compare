package jflex;

import java.util.Queue;
import java.util.LinkedList;

/*
 * JFlex lexer for converting Tree-sitter leaf nodes to JavaToken
 * Based on Java 1.2+ language features, aligned with tree-sitter-java grammar
 */

%%

%public
%class TreeSitterLexer

%unicode

%line
%column
%type JavaToken

%{
  // Очередь для накопления токенов в случае составных правил
  private Queue<JavaToken> tokenQueue = new LinkedList<>();

  // Метод для получения следующего токена с учётом очереди
  public JavaToken nextToken() throws java.io.IOException {
    if (!tokenQueue.isEmpty()) {
      return tokenQueue.poll();
    }
    return yylex();
  }
%}

/* Основные классы символов */
LeafNode = [^\ \t\n\r\f]+    // Тип ноды Tree-sitter — любая строка без пробелов
WhiteSpace = [ \t\n\r\f]+    // Пробельные символы как разделители

/* Состояния для обработки составных последовательностей */
%state STRING_LITERAL

%%

<YYINITIAL> {

  /* Ключевые слова и идентификаторы */
  "abstract"         { return JavaToken.ABSTRACT; }
  "boolean_type"     { return JavaToken.BOOLEAN; }
  "break"            { return JavaToken.BREAK; }
  "byte"             { return JavaToken.BYTE; }
  "case"             { return JavaToken.CASE; }
  "catch"            { return JavaToken.CATCH; }
  "char"             { return JavaToken.CHAR; }
  "class"            { return JavaToken.CLASS; }
  "const"            { return JavaToken.CONST; }
  "continue"         { return JavaToken.CONTINUE; }
  "default"          { return JavaToken.DEFAULT; }
  "do"               { return JavaToken.DO; }
  "double"           { return JavaToken.DOUBLE; }
  "else"             { return JavaToken.ELSE; }
  "enum"             { return JavaToken.ENUM; }
  "extends"          { return JavaToken.EXTENDS; }
  "final"            { return JavaToken.FINAL; }
  "finally"          { return JavaToken.FINALLY; }
  "float"            { return JavaToken.FLOAT; }
  "for"              { return JavaToken.FOR; }
  "if"               { return JavaToken.IF; }
  "implements"       { return JavaToken.IMPLEMENTS; }
  "import"           { return JavaToken.IMPORT; }
  "instanceof"       { return JavaToken.INSTANCEOF; }
  "int"              { return JavaToken.INT; }
  "interface"        { return JavaToken.INTERFACE; }
  "long"             { return JavaToken.LONG; }
  "native"           { return JavaToken.NATIVE; }
  "new"              { return JavaToken.NEW; }
  "package"          { return JavaToken.PACKAGE; }
  "private"          { return JavaToken.PRIVATE; }
  "protected"        { return JavaToken.PROTECTED; }
  "public"           { return JavaToken.PUBLIC; }
  "return"           { return JavaToken.RETURN; }
  "short"            { return JavaToken.SHORT; }
  "static"           { return JavaToken.STATIC; }
  "strictfp"         { return JavaToken.STRICTFP; }
  "super"            { return JavaToken.SUPER; }
  "switch"           { return JavaToken.SWITCH; }
  "synchronized"     { return JavaToken.SYNCHRONIZED; }
  "this"             { return JavaToken.THIS; }
  "throw"            { return JavaToken.THROW; }
  "throws"           { return JavaToken.THROWS; }
  "transient"        { return JavaToken.TRANSIENT; }
  "try"              { return JavaToken.TRY; }
  "void_type"        { return JavaToken.VOID; }
  "volatile"         { return JavaToken.VOLATILE; }
  "while"            { return JavaToken.WHILE; }

  /* Литералы */
  "true"             { return JavaToken.BOOLEAN_LITERAL; }
  "false"            { return JavaToken.BOOLEAN_LITERAL; }
  "null_literal"     { return JavaToken.NULL_LITERAL; }
  "decimal_integer_literal" { return JavaToken.INTEGER_LITERAL; }
  "hex_integer_literal"     { return JavaToken.INTEGER_LITERAL; }
  "octal_integer_literal"   { return JavaToken.INTEGER_LITERAL; }
  "binary_integer_literal"  { return JavaToken.INTEGER_LITERAL; }
  "decimal_floating_point_literal" { return JavaToken.FLOATING_POINT_LITERAL; }
  "hex_floating_point_literal"     { return JavaToken.FLOATING_POINT_LITERAL; }
  "character_literal"       { return JavaToken.CHARACTER_LITERAL; }

  /* Разделители */
  "("                { return JavaToken.LPAREN; }
  ")"                { return JavaToken.RPAREN; }
  "{"                { return JavaToken.LBRACE; }
  "}"                { return JavaToken.RBRACE; }
  "["                { return JavaToken.LBRACK; }
  "]"                { return JavaToken.RBRACK; }
  ";"                { return JavaToken.SEMICOLON; }
  ","                { return JavaToken.COMMA; }
  "."                { return JavaToken.DOT; }
  "..."              { return JavaToken.ELLIPSIS; }
  "::"               { return JavaToken.COLONCOLON; }

  /* Операторы */
  "="                { return JavaToken.EQ; }
  ">"                { return JavaToken.GT; }
  "<"                { return JavaToken.LT; }
  "!"                { return JavaToken.NOT; }
  "~"                { return JavaToken.COMP; }
  "?"                { return JavaToken.QUESTION; }
  ":"                { return JavaToken.COLON; }
  "->"               { return JavaToken.ARROW; }
  "=="               { return JavaToken.EQEQ; }
  "<="               { return JavaToken.LTEQ; }
  ">="               { return JavaToken.GTEQ; }
  "!="               { return JavaToken.NOTEQ; }
  "&&"               { return JavaToken.ANDAND; }
  "||"               { return JavaToken.OROR; }
  "++"               { return JavaToken.PLUSPLUS; }
  "--"               { return JavaToken.MINUSMINUS; }
  "+"                { return JavaToken.PLUS; }
  "-"                { return JavaToken.MINUS; }
  "*"                { return JavaToken.MULT; }
  "/"                { return JavaToken.DIV; }
  "&"                { return JavaToken.AND; }
  "|"                { return JavaToken.OR; }
  "^"                { return JavaToken.XOR; }
  "%"                { return JavaToken.MOD; }
  "+="               { return JavaToken.PLUSEQ; }
  "-="               { return JavaToken.MINUSEQ; }
  "*="               { return JavaToken.MULTEQ; }
  "/="               { return JavaToken.DIVEQ; }
  "&="               { return JavaToken.ANDEQ; }
  "|="               { return JavaToken.OREQ; }
  "^="               { return JavaToken.XOREQ; }
  "%="               { return JavaToken.MODEQ; }
  "<<="              { return JavaToken.LSHIFTEQ; }
  ">>="              { return JavaToken.RSHIFTEQ; }
  ">>>="             { return JavaToken.URSHIFTEQ; }

  /* Идентификаторы */
  "identifier"       { return JavaToken.IDENTIFIER; }
  "type_identifier"  { return JavaToken.IDENTIFIER; }

  /* Комментарии — игнорируем */
  "line_comment"     { /* ignore */ }
  "block_comment"    { /* ignore */ }

  /* Специальные случаи */
  "@"                { return JavaToken.AT; }
  "@interface"       { tokenQueue.add(JavaToken.INTERFACE); return JavaToken.AT; }
  "\""               { yybegin(STRING_LITERAL); }

  /* Пробельные символы */
  {WhiteSpace}       { /* ignore */ }

  /* Конец ввода */
  "end"              { return JavaToken.EOF; }
}

<STRING_LITERAL> {
  "string_fragment"  { return JavaToken.STRING_LITERAL; }
  "\""               { yybegin(YYINITIAL); /* ignore closing quote if no string_fragment */ }
  {WhiteSpace}       { /* ignore */ }
  {LeafNode}         { yybegin(YYINITIAL); return JavaToken.ERROR; }  // Фиктивный токен вместо ошибки
}

/* Неизвестные ноды */
{LeafNode}           { return JavaToken.ERROR; }  // Фиктивный токен вместо ошибки

<<EOF>>              { return JavaToken.EOF; }