grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
// TODO: Hier kan een nummer komen als id, was een vraag bij veel assessments.
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;
MIXED_IDENT: [a-zA-Z] [a-zA-Z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
//level 0
//--- PARSER: ---

// level 0
stylesheet: variableAssignment* stylerule+;
stylerule: selector OPEN_BRACE (declaratie | ifClause)+ CLOSE_BRACE;
selector: ID_IDENT | CLASS_IDENT | LOWER_IDENT;
declaratie: property COLON expression SEMICOLON;
property: LOWER_IDENT;

variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;
variableReference: LOWER_IDENT | CAPITAL_IDENT | MIXED_IDENT;

expression
    : literal                       #LiteralExpression
    | expression MUL expression     #MultiplyExpression
    | expression MIN expression     #SubstractExpression
    | expression PLUS expression    #AddOperation
    ;

literal: COLOR | PIXELSIZE | PERCENTAGE | SCALAR | booleanLiteral | variableReference;
booleanLiteral: TRUE | FALSE ;

ifClause: IF BOX_BRACKET_OPEN (booleanLiteral | variableReference) BOX_BRACKET_CLOSE OPEN_BRACE (declaratie | variableAssignment | ifClause)+ CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE (declaratie | variableAssignment)+ CLOSE_BRACE;


