grammar GML;

tokens {
	SQBRL 	= '[' ;
	SQBRR	= ']' ;
	DQUOTE	= '"' ;
}

@parser::header {
package fr.inria.zuist.app.wm;
}

@lexer::header {
package fr.inria.zuist.app.wm;
}

@members {
    public static void main(String[] args) throws Exception {
        GMLLexer lex = new GMLLexer(new ANTLRFileStream(args[0]));
       	CommonTokenStream tokens = new CommonTokenStream(lex);

        GMLParser parser = new GMLParser(tokens);

        try {
            parser.expr();
        } catch (RecognitionException e)  {
            e.printStackTrace();
        }
    }
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

expr	: graph ;

graph : GRAPH SQBRL meta (node|edge)* SQBRR ;

meta : (directed|version)*;

version : VERSION NUMBER ;

directed : DIRECTED NUMBER ;

node : NODE SQBRL id airport cityname x y SQBRR;

edge : EDGE SQBRL source target id weight length SQBRR;

id: ID NUMBER ;

airport : AIRPCODE DQUOTE NAME DQUOTE;

cityname : CITYNAME DQUOTE NAME DQUOTE ;

x : X NUMBER ;

y : Y NUMBER ;

source : SOURCE NUMBER ;

target : TARGET NUMBER ;

weight : WEIGHT NUMBER ;

length : LENGTH NUMBER ;

/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

GRAPH : 'graph' ;

DIRECTED : 'directed' ;

VERSION : 'version' ;

NODE : 'node' ;

ID : 'id' ;

AIRPCODE : 'airport_code' ;

CITYNAME : 'city_name' ;

X : 'x' ;
Y : 'y' ;

EDGE : 'edge' ;

SOURCE : 'source' ;
TARGET : 'target' ;
WEIGHT : 'weight' ;
LENGTH : 'length' ;

NUMBER : ('-')? (DIGIT)+ ('.' (DIGIT)+)? ;

NAME : ( LETTER | '_' | '-' | '\'' | '/' | '.' | '?' | '(' | ')' )+ ;

WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ 	{ $channel = HIDDEN; } ;

fragment DIGIT	: '0'..'9' ;

fragment LETTER : ('a'..'z' | 'A'..'Z') ;
