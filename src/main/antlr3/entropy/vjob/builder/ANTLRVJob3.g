//
// Copyright (c) 2010 Ecole des Mines de Nantes.
// 
//  This file is part of Entropy.
// 
//  Entropy is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
// 
//  Entropy is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with Entropy.  If not, see <http://www.gnu.org/licenses/>.
//
grammar ANTLRVJob3;

options {
	language = Java;
	output = AST;
	ASTLabelType=VJobTree;
//	backtrack = true;
}

@lexer::header {
package entropy.vjob.builder;
}
/*
@lexer::members {
  @Override
  public void reportError(RecognitionException e) {
    throw new IllegalArgumentException(e);
  }
}*/
@parser::header {
	package entropy.vjob.builder;
}
//Set and constraint declaration
VAR  :	'$' ('a'..'z'|'A'..'Z') (('a'..'z'|'A'..'Z'|'0'..'9'|'_')*('a'..'z'|'A'..'Z'|'0'..'9'))?;
VAL	:	('a'..'z'|'A'..'Z')(('a'..'z'|'A'..'Z'|'0'..'9'|'-'|'_'|'.')*('a'..'z'|'A'..'Z'|'0'..'9'))?;
CNAME	:	('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')*'(';

//Exploded set
INT 	:	'0'..'9'+;

//Go for the interval
LVAL 	:	('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'-'|'_'|'.')*'[';
RVAL 	:	']'(('a'..'z'|'A'..'Z'|'0'..'9'|'-'|'_'|'.')*('a'..'z'|'A'..'Z'|'0'..'9'))?;

//Parenthesis for the constraints parameters

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

EXPLODED_SET	:'{}'	;
INTERVAL:'..'	;
EXPLODED_INTERVAL:	',';
EQUALS	:	'=';
UNION	:	'+';
DIFF	:	'-';

STR	:	('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'-'|'_'|'.')+;

range_of_elements: LVAL INT '..' INT RVAL -> ^(INTERVAL LVAL INT INT RVAL);
exploded_range:	LVAL INT (',' INT)+ RVAL ->^(EXPLODED_INTERVAL LVAL INT+ RVAL);													
exploded_set :'{' elem2 (',' elem2)* '}' -> elem2+;

operand	:'+'
	|'-';
			

elem2	: atom (operand^ elem2)?;	
	
atom	:VAR
	| INT
	| VAL
	|'(' elem2 ')' -> elem2
	|range_of_elements
	|exploded_range
	| exploded_set  -> ^(EXPLODED_SET exploded_set);

var_decl:VAR '=' elem2 ';' -> ^(EQUALS VAR elem2);

constraint :	CNAME elem2 (',' elem2)* ')' ';' -> ^(CNAME elem2+);	

//foreach	:'foreach' VAR 'in' atom '{' vjob_decl '}';
vjob_decl: (var_decl|constraint/*|foreach*/)*;
