//
// Copyright (c) 2009 Ecole des Mines de Nantes.
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
grammar ANTLRDurationEvaluator;

options {
	language = Java;
	output = AST;	
}

@lexer::header {
package entropy.plan.durationEvaluator;
}
@parser::header {
package entropy.plan.durationEvaluator;
import entropy.configuration.VirtualMachine;
import entropy.configuration.Node;
}

@parser::members {
	private VirtualMachine vm;
	private Node n;
	public int evaluate(VirtualMachine v) throws org.antlr.runtime.RecognitionException {
		if (v == null) {
			return -1;
		}
		vm = v;		
		return new Double(vm_expr().value).intValue();
	}
	
	public int evaluate(Node node) throws org.antlr.runtime.RecognitionException {
		if (node == null) {
			return -1;
		}
		n = node;
		return new Double(node_expr().value).intValue();
	}
}

PLUS 	:	'+';
MINUS	:	'-';
MULTIPLY:	'*';
DIV	:	'/';
MOD	:	'%';
POW	:	'^';
LPARA:	'(';
RPARA:	')';


VM_ID:'VM';
NODE_ID	:'node';
SUB:'#';
MEMORY:'memory';
CPU_DEMAND:'cpu_demand';
CPU_CONS:'cpu_cons';
CPU_NB:'cpu_nb';
CPU_CAPA:	'cpu_capa';
INT :	'0'..'9'+;

FLOAT  :   ('0'..'9')+ '.' ('0'..'9')+ ;

WS  :   (' ') {$channel=HIDDEN;};
    
node_expression returns [double value]: e=node_expr {$value = $e.value;};
node_expr returns [double value]: e=node_multiplicative_expr {$value = $e.value;} 
	(
	PLUS e=node_multiplicative_expr{$value += $e.value;}
	|MINUS e=node_multiplicative_expr{$value -= $e.value;}
	)*;
node_multiplicative_expr returns [double value]
	: e=node_unary_expr {$value=$e.value;} 
	(MULTIPLY e=node_unary_expr {$value *=$e.value;}
	|DIV e=node_unary_expr {$value /= $e.value;}
	)*;
node_unary_expr returns [double value]:
	INT {$value = Integer.parseInt($INT.text);}
	|FLOAT {$value = Double.parseDouble($FLOAT.text);}
	| NODE_ID SUB MEMORY {$value = n.getMemoryCapacity();}
	| NODE_ID SUB CPU_CAPA {$value = n.getCPUCapacity();}
	| NODE_ID SUB CPU_NB {$value = n.getNbOfCPUs();}
	| LPARA node_expr RPARA {$value = $node_expr.value;}
	;
	
vm_expression returns [double value]: e=vm_expr {$value = $e.value;};
vm_expr returns [double value]: e=vm_multiplicative_expr {$value = $e.value;} 
	(
	PLUS e=vm_multiplicative_expr{$value += $e.value;}
	|MINUS e=vm_multiplicative_expr{$value -= $e.value;}
	)*;
vm_multiplicative_expr returns [double value]
	: e=vm_unary_expr {$value=$e.value;} 
	(MULTIPLY e2=vm_unary_expr {$value *=$e2.value;}
	|DIV e2=vm_unary_expr {$value /= $e2.value;}
	)*;
vm_unary_expr returns [double value]:
	INT {$value = Integer.parseInt($INT.text);}
	|FLOAT {$value = Double.parseDouble($FLOAT.text);}
	| VM_ID SUB MEMORY {$value = vm.getMemoryConsumption();}
	| VM_ID SUB CPU_DEMAND {$value = vm.getCPUConsumption();}
	| VM_ID SUB CPU_CONS {$value = vm.getCPUConsumption();}
	| VM_ID SUB CPU_NB {$value = vm.getNbOfCPUs();}
	| LPARA vm_expr RPARA {$value = $vm_expr.value;}
	;