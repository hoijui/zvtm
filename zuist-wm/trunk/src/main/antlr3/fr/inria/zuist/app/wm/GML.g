/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

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

node : NODE SQBRL (attrib)* SQBRR;

edge : EDGE SQBRL (attrib)* SQBRR;

attrib : NAME (NUMBER | DQUOTE NAME DQUOTE) ;



/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

GRAPH : 'graph' ;

DIRECTED : 'directed' ;

VERSION : 'version' ;

NODE : 'node' ;

EDGE : 'edge' ;

NUMBER : ('-')? (DIGIT)+ ('.' (DIGIT)+)? ;

NAME : ( LETTER | '_' | '-' | '\'' | '/' | '.' | '?' | '(' | ')' )+ ;

WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ 	{ $channel = HIDDEN; } ;

fragment DIGIT	: '0'..'9' ;

fragment LETTER : ('a'..'z' | 'A'..'Z') ;
