/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

grammar GML;

options {
  output = AST;
}

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
            parser.gmlgr();
        } catch (RecognitionException e)  {
            e.printStackTrace();
        }
    }
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

gmlgr	: graph EOF!;

graph : GRAPH SQBRL meta elements SQBRR ;

meta : (directed|version)* ;

version : VERSION NUMBER -> ^(VERSION NUMBER);

directed : DIRECTED NUMBER -> ^(DIRECTED NUMBER) ;

elements : (element)* ;

element : node | edge ;

node : NODE SQBRL attribs SQBRR -> ^(NODE attribs);

edge : EDGE SQBRL attribs SQBRR -> ^(EDGE attribs);

attribs : (attrib)* ;

attrib : NAME value -> ^(NAME value) ;

value: (NUMBER | QUOTED_NAME) ;

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

QUOTED_NAME : '"' NAME '"' ;

WHITESPACE : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ 	{ $channel = HIDDEN; } ;

fragment DIGIT	: '0'..'9' ;

fragment LETTER : ('a'..'z' | 'A'..'Z') ;
