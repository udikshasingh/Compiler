/**
 * Parser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2020.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2020 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2020
 *
 */


package cop5556fa20;

import static cop5556fa20.Scanner.Kind.AND;
import static cop5556fa20.Scanner.Kind.ASSIGN;
import static cop5556fa20.Scanner.Kind.AT;
import static cop5556fa20.Scanner.Kind.COLON;
import static cop5556fa20.Scanner.Kind.COMMA;
import static cop5556fa20.Scanner.Kind.CONST;
import static cop5556fa20.Scanner.Kind.DIV;
import static cop5556fa20.Scanner.Kind.EOF;
import static cop5556fa20.Scanner.Kind.EQ;
import static cop5556fa20.Scanner.Kind.EXCL;
import static cop5556fa20.Scanner.Kind.GE;
import static cop5556fa20.Scanner.Kind.GT;
import static cop5556fa20.Scanner.Kind.HASH;
import static cop5556fa20.Scanner.Kind.IDENT;
import static cop5556fa20.Scanner.Kind.INTLIT;
import static cop5556fa20.Scanner.Kind.KW_GREEN;
import static cop5556fa20.Scanner.Kind.KW_HEIGHT;
import static cop5556fa20.Scanner.Kind.KW_RED;
import static cop5556fa20.Scanner.Kind.KW_BLUE;
import static cop5556fa20.Scanner.Kind.KW_SCREEN;
import static cop5556fa20.Scanner.Kind.KW_WIDTH;
import static cop5556fa20.Scanner.Kind.KW_X;
import static cop5556fa20.Scanner.Kind.KW_Y;
import static cop5556fa20.Scanner.Kind.KW_image;
import static cop5556fa20.Scanner.Kind.KW_int;
import static cop5556fa20.Scanner.Kind.KW_string;
import static cop5556fa20.Scanner.Kind.LARROW;
import static cop5556fa20.Scanner.Kind.LE;
import static cop5556fa20.Scanner.Kind.LPAREN;
import static cop5556fa20.Scanner.Kind.LPIXEL;
import static cop5556fa20.Scanner.Kind.LSQUARE;
import static cop5556fa20.Scanner.Kind.LT;
import static cop5556fa20.Scanner.Kind.MINUS;
import static cop5556fa20.Scanner.Kind.MOD;
import static cop5556fa20.Scanner.Kind.NEQ;
import static cop5556fa20.Scanner.Kind.NOP;
import static cop5556fa20.Scanner.Kind.OR;
import static cop5556fa20.Scanner.Kind.PLUS;
import static cop5556fa20.Scanner.Kind.Q;
import static cop5556fa20.Scanner.Kind.RSQUARE;
import static cop5556fa20.Scanner.Kind.SEMI;
import static cop5556fa20.Scanner.Kind.STAR;
import static cop5556fa20.Scanner.Kind.STRINGLIT;
import static cop5556fa20.Scanner.Kind.RARROW;
import static cop5556fa20.Scanner.Kind.RPAREN;
import static cop5556fa20.Scanner.Kind.RPIXEL;



import java.util.ArrayList;
import java.util.List;

import cop5556fa20.Scanner.Kind;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.Scanner.Token;
import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.Dec;
import cop5556fa20.AST.DecImage;
import cop5556fa20.AST.DecVar;
import cop5556fa20.AST.ExprArg;
import cop5556fa20.AST.ExprBinary;
import cop5556fa20.AST.ExprConditional;
import cop5556fa20.AST.ExprConst;
import cop5556fa20.AST.ExprHash;
import cop5556fa20.AST.ExprIntLit;
import cop5556fa20.AST.ExprPixelConstructor;
import cop5556fa20.AST.ExprPixelSelector;
import cop5556fa20.AST.ExprStringLit;
import cop5556fa20.AST.ExprUnary;
import cop5556fa20.AST.ExprVar;
import cop5556fa20.AST.Expression;
import cop5556fa20.AST.Program;
import cop5556fa20.AST.Statement;
import cop5556fa20.AST.StatementAssign;
import cop5556fa20.AST.StatementImageIn;
import cop5556fa20.AST.StatementLoop;
import cop5556fa20.AST.StatementOutFile;
import cop5556fa20.AST.StatementOutScreen;
import cop5556fa20.AST.Type;

public class Parser {

	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		final Token token;  //the token that caused an error to be discovered.

		public SyntaxException(Token token, String message) {
			super(message);
			this.token = token;
		}

		public Token token() {
			return token;
		}

	}


	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken(); // establish invariant that t is always the next token to be processed
	}

	public Program parse() throws SyntaxException, LexicalException {
		Program p = program();
		matchEOF();
		return p;
	}

	private static final Kind[] firstProgram = {KW_int, KW_string, IDENT, KW_image}; //this is not the correct FIRST(Program...), but illustrates a handy programming technique
	private static final Kind[] firstExpression = {PLUS, MINUS, EXCL, INTLIT, IDENT, LPAREN, STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT};
	//private static final Kind[] firstconstXYSelector = {LSQUARE, KW_X, COMMA, KW_Y, RSQUARE};
	private static final Kind[] firstattribute = {KW_WIDTH, KW_HEIGHT, KW_RED, KW_BLUE, KW_GREEN, RSQUARE};
	private static final Kind[] firstprimary = {INTLIT, IDENT, LPAREN, STRINGLIT, KW_X, KW_Y, CONST, LPIXEL, AT};
	
	private Program program() throws SyntaxException, LexicalException {
		Token first = t; 
		List<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		while (isKind(firstProgram)) {
			switch (t.kind()) {
			case KW_int, KW_string, KW_image -> {
				Dec dec = declaration();
				decsAndStatements.add(dec);
				match(SEMI);
			}
			
			case IDENT -> {
				Statement statement = statement();
				decsAndStatements.add(statement);
				match(SEMI);
			}
			default -> throw new SyntaxException(t, "unimplemented feature in program"); 
			}
		}
		//if (t.kind() != EOF)
			//throw new SyntaxException(t, "Invalid syntax");
		return new Program(first, decsAndStatements);  
	}

	private Dec declaration() throws SyntaxException, LexicalException {
		Token first = t;  
		if (isKind(KW_int)) {
			consume();
			Type type = Type.Int;
			Token name = match(IDENT);
			Expression e = Expression.empty;  
			                                 
			if (isKind(ASSIGN)) {
				consume();
				e = expression();
			}
			return new DecVar(first, type , scanner.getText(name), e);  
		}
		
		else if (isKind(KW_string)) {
			consume();
			Type type = Type.String;
			Token name = match(IDENT);
			Expression e = Expression.empty;
			
			if (isKind(ASSIGN)) {
				consume();
				e = expression();
			}
			
			return new DecVar(first, type, scanner.getText(name), e);
		}
		
		else if (isKind(KW_image)) {
			consume();
			Type type = Type.Image;
			Expression e0 = Expression.empty;
			Expression e1 = Expression.empty;
			Expression e2 = Expression.empty;
			
			Kind op = NOP;
			
			if (isKind(LSQUARE)) {
				consume();
				e0 = expression();
				match(COMMA);
				e1 = expression();
				match(RSQUARE);
			}
			
			Token name = match(IDENT);
			
			if (isKind(LARROW) || isKind(ASSIGN)) {
				op = (isKind(LARROW)) ? LARROW : ASSIGN;
				consume();
				e2 = expression();
			}
			
			return new DecImage(first, type, scanner.getText(name), e0, e1, op, e2);
		}
		
		else {
			throw new SyntaxException(t, "no match found for token in Declaration");
		}
		//return null; 
	}
	
	private Statement statement() throws SyntaxException, LexicalException {
		Token first = t;
		Token name = match(IDENT);
		Statement statement = switch (t.kind()) {
		case RARROW -> {
			consume();
			if (isKind(KW_SCREEN)) {
				consume();
				Expression e0 = Expression.empty;
				Expression e1 = Expression.empty;
				if (isKind(LSQUARE)) {
					consume();
					e0 = expression();
					match(COMMA);
					e1 = expression();
					match(RSQUARE);
				}
				
				yield new StatementOutScreen(first, scanner.getText(name), e0, e1);
			}
			Expression e = expression();
			yield new StatementOutFile(first, scanner.getText(name), e);
		}
		case LARROW -> {
			consume();
			Expression e = expression();
			yield new StatementImageIn(first, scanner.getText(name), e);
		}
		case ASSIGN -> {
			consume();
			if (isKind(STAR)) {
				consume();
				constXYSelector();
				match(COLON);
				Expression e0 = Expression.empty;
				if (isKind(firstExpression)) {
					e0 = expression();
				}
				match(COLON);
				Expression e1 = expression();
				yield new StatementLoop(first, scanner.getText(name), e0, e1);
			}
			Expression e = expression();
			yield new StatementAssign(first, scanner.getText(name), e);
		}
		default -> throw new SyntaxException(t, "Unexpected value of token ");
		};
		return statement;
	}

	protected void constXYSelector() throws SyntaxException, LexicalException {
		match(LSQUARE);
		match(KW_X);
		match(COMMA);
		match(KW_Y);
		match(RSQUARE);
	}
	
	protected Expression expression() throws SyntaxException, LexicalException {
		
		Token first = t;
		if (isKind(firstExpression)) {
			Expression e0 = orExpression();
			Expression e1 = Expression.empty;
			Expression e2 = Expression.empty;
			if (isKind(Q)) {
				consume();
				e1 = expression();
				match(COLON);
				e2 = expression();
				e0 = new ExprConditional(first, e0, e1, e2);
			}
			
			return e0;
		}
		else 
			throw new SyntaxException(t, "Not a valid start of expression");
		
	}
	
	protected Expression orExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = andExpression();
		while (isKind(OR)) {
			Kind op = OR;
			consume();
			Expression e1 = andExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	protected Expression andExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = eqExpression();
		while (isKind(AND)) {
			Kind op = AND;
			consume();
			Expression e1 = eqExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	protected Expression eqExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = relExpression();
		while (isKind(EQ) || isKind(NEQ)) {
			Kind op = (isKind(EQ)) ? EQ : NEQ;
			consume();
			Expression e1 = relExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	protected Expression relExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = addExpression();
		
		while (isKind(LT) || isKind(GT) || isKind(LE) || isKind(GE)) {
			Kind op = LT;
			if (isKind(GT))
				op = GT;
			else if (isKind(LE))
				op = LE;
			else if (isKind(GE))
				op = GE;
			consume();
			Expression e1 = addExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	protected Expression addExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = multExpression();
		while (isKind(PLUS) || isKind(MINUS)) {
			Kind op = (isKind(PLUS) ? PLUS : MINUS);
			consume();
			Expression e1 = multExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	protected Expression multExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression e0 = unaryExpression();
		while (isKind(STAR) || isKind(DIV) || isKind(MOD)) {
			Kind op = STAR;
			if (isKind(DIV))
				op = DIV;
			else if (isKind(MOD))
				op = MOD;
			consume();
			Expression e1 = unaryExpression();
			e0 = new ExprBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	protected Expression unaryExpression() throws SyntaxException, LexicalException {
		Token first = t;
		if (isKind(PLUS) || isKind(MINUS)) {
			Kind op = (isKind(PLUS) ? PLUS : MINUS);
			consume();
			Expression e = unaryExpression();
			return new ExprUnary(first, op, e);
		}
		 
		return	unaryExpressionNotPlusMinus();
	}
	
	protected Expression unaryExpressionNotPlusMinus() throws SyntaxException, LexicalException {
		Token first = t;
		if (isKind(EXCL)) {
			Kind op = EXCL;
			consume();
			Expression e = unaryExpression();
			return new ExprUnary(first, op, e);
		}
		
		return hashExpression();
	}
	
	protected Expression hashExpression() throws SyntaxException, LexicalException {
		Token first = t;
		Expression 	e = primary();
		while (isKind(HASH)) {
			consume();
			Token attribute = match(firstattribute);
			e = new ExprHash(first, e, scanner.getText(attribute));
		}
		return e;
	}
	

	private Expression primary() throws SyntaxException, LexicalException {
		
		if (isKind(firstprimary)) {
			Token first = t;	
			
			Expression e = switch (t.kind()) {
			case INTLIT -> {
				int value = scanner.intVal(t);
				consume();
				yield new ExprIntLit(first, value);
			}
			case IDENT -> {
				Token name = match(IDENT);
				yield new ExprVar(first, scanner.getText(name));
			}
			case LPAREN -> {
				consume();
				Expression  expr = expression();
				match(RPAREN);
				yield expr;
			}
			case STRINGLIT -> {
				String text = scanner.getText(t);
				consume();
				yield new ExprStringLit(first, text);
			}
			case KW_X, KW_Y -> {
				Token name;
				if (isKind(KW_X))
					name = match(KW_X);
				else
					name = match(KW_Y);
				yield new ExprVar(first, scanner.getText(name));
			}
			case CONST -> {
				int value = scanner.intVal(t);
				Token name = match(CONST);
				yield new ExprConst(first, scanner.getText(name), value);
			}
			case LPIXEL -> {
				consume();
				Expression er = expression();
				match(COMMA);
				Expression eg = expression();
				match(COMMA);
				Expression eb = expression();
				match(RPIXEL);
				yield new ExprPixelConstructor(first, er, eg, eb);
			}
			case AT -> {
				consume();
				Expression ep = primary();
				yield new ExprArg(first, ep);
			}

			default -> throw new SyntaxException(t, "Error or unimplemented feature: ");
			};
			
			if (isKind(LSQUARE)) {
				consume();
				Expression ex = expression();
				match(COMMA);
				Expression ey = expression();
				match(RSQUARE);
				e = new ExprPixelSelector(first, e, ex, ey);
			}
			return e;
		}
		
		else 
			throw new SyntaxException(t, "illegal start of Primary");
		
	}


	protected boolean isKind(Kind kind) {
		return t.kind() == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind())
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(t, kind.toString());
		return null; // unreachable
	}

	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		error(t, "expected one of " + kinds);
		return null; // unreachable
	}

	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			error(t, "attempting to consume EOF");
		}
		t = scanner.nextToken();
		return tmp;
	}

	private void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line() + ":" + t.posInLine();
		throw new SyntaxException(t, message);
	}
	
	/**
	 * Only for check at end of program. Does not "consume" EOF so there is no
	 * attempt to get the nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		error(t, EOF.toString());
		return null; // unreachable
	}
}
