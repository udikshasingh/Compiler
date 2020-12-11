package cop5556fa20;

import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.ASTVisitor;
import cop5556fa20.AST.Dec;
import cop5556fa20.AST.DecImage;
import cop5556fa20.AST.DecVar;
import cop5556fa20.AST.ExprArg;
import cop5556fa20.AST.ExprBinary;
import cop5556fa20.AST.ExprConditional;
import cop5556fa20.AST.ExprConst;
import cop5556fa20.AST.ExprEmpty;
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
import static cop5556fa20.Scanner.Kind.LARROW;
import static cop5556fa20.Scanner.Kind.ASSIGN;
import static cop5556fa20.Scanner.Kind.NOP;
import static cop5556fa20.Scanner.Kind.AND;
import static cop5556fa20.Scanner.Kind.OR;
import static cop5556fa20.Scanner.Kind.EQ;
import static cop5556fa20.Scanner.Kind.NEQ;
import static cop5556fa20.Scanner.Kind.LT;
import static cop5556fa20.Scanner.Kind.GT;
import static cop5556fa20.Scanner.Kind.LE;
import static cop5556fa20.Scanner.Kind.GE;
import static cop5556fa20.Scanner.Kind.PLUS;
import static cop5556fa20.Scanner.Kind.MINUS;
import static cop5556fa20.Scanner.Kind.EXCL;
import static cop5556fa20.Scanner.Kind.KW_int;
import static cop5556fa20.Scanner.Kind.KW_string;
import static cop5556fa20.Scanner.Kind.KW_image;
import static cop5556fa20.Scanner.Kind.KW_X;
import static cop5556fa20.Scanner.Kind.KW_Y;


import java.util.HashMap;
import java.util.Map;

import cop5556fa20.Scanner.Token;

public class TypeCheckVisitor implements ASTVisitor {
	
	Map<String, Dec> symbolTable_Dec; 
	boolean hasArgsExpr = false;

	@SuppressWarnings("serial")
	class TypeException extends Exception {
		Token first;
		String message;
		
		public TypeException(Token first, String message) {
			super();
			this.first = first;
			this.message = "Semantic error:  "+first.line() + ":" + first.posInLine() + " " +message;
		}
		
		public String toString() {
			return message;
		}	
	}
	
	
	public TypeCheckVisitor() {
		super();
		symbolTable_Dec = new HashMap<String,Dec>();
	}
	

	@Override
	public Object visitDecImage(DecImage decImage, Object arg) throws Exception {
		if(!symbolTable_Dec.containsKey(decImage.name)) {
			symbolTable_Dec.put(decImage.name, decImage);
		}
		else {
			throw new TypeException(decImage.first, "DecImage already declared before" );
		}
		decImage.source.setExpectedType(Type.String);
		Expression e0 = (Expression) decImage.width.visit(this, arg);
		Expression e1 = (Expression) decImage.height.visit(this, arg);
		Expression e2 = (Expression) decImage.source.visit(this, arg);
		
		
		if(!(e0.type().equals(Type.Int) || e0.type().equals(Type.Void))){
		    
			throw new TypeException(decImage.first, "Expression0 should be Int or Void type" );
		}
		
		if(!(e1.type().equals(Type.Int) || e1.type().equals(Type.Void))){
			throw new TypeException(decImage.first, "Expression1 should be Int or Void type" );
		}
		
		if (!e0.type().equals(e1.type())) {
			throw new TypeException(decImage.first, "Expression0 type not equal to Expression1" );
		}
		
		if (decImage.op() == LARROW) {
			if (!e2.type().equals(Type.String) && !e2.type().equals(Type.Image))
				throw new TypeException(decImage.first, "OP == LARROW : Expression2.type should be String or Image" );
		}
		
		else if (decImage.op() == ASSIGN) {
			if (!e2.type().equals(Type.Image))
				throw new TypeException(decImage.first, "OP == ASSIGN, but Expression2's type is not equal to Image" );
		}
		
		else if (decImage.op() != NOP) {
			throw new TypeException(decImage.first, "OP should be NOP, but isn't" );
		}
		hasArgsExpr = false;
		return decImage;
	}
	

	@Override
	public Object visitDecVar(DecVar decVar, Object arg) throws Exception {
		
		if(!symbolTable_Dec.containsKey(decVar.name)) {
			symbolTable_Dec.put(decVar.name, decVar);
			
			decVar.expression.setExpectedType(decVar.type());
			Expression e = (Expression)decVar.expression.visit(this, arg);
			hasArgsExpr = false;
			if (e.type() == Type.Void || e.type().equals(decVar.type())) {
				return decVar;
			}
		}
		
			throw new TypeException(decVar.first, "DecVar already declared before");
	}

	
	@Override
	public Object visitExprArg(ExprArg exprArg, Object arg) throws Exception {
		hasArgsExpr = true;
		Expression e = (Expression)exprArg.e.visit(this, arg);
		
		if (e.type() != Type.Int) {
			throw new TypeException(exprArg.first, "Type mismatch from visitExprArg");
		}
		
		System.out.println(exprArg.expectedType());
		if (exprArg.expectedType() != Type.Int && exprArg.expectedType() != Type.String) {
			throw new TypeException(exprArg.first, "Type mismatch from visitExprArg");
		}
		
		exprArg.setType(exprArg.expectedType());
		
		return exprArg;
	}
	

	@Override
	public Object visitExprBinary(ExprBinary exprBinary, Object arg) throws Exception {
		
		Expression e0 = (Expression)exprBinary.e0.visit(this, arg);
		Expression e1 = (Expression)exprBinary.e1.visit(this, arg);
		
		if (exprBinary.op() == AND || exprBinary.op() == OR) {
			if (!(e0.type() == Type.Boolean && e1.type() == Type.Boolean)) {
				throw new TypeException(exprBinary.first, "Type mismatch from ExprBinary");
			}
			if (hasArgsExpr) {
				throw new TypeException(exprBinary.first, "AND, OR operation not allowed with expr args");
			}
			exprBinary.setType(Type.Boolean);
			return exprBinary;
		}
		
		if (exprBinary.op() == EQ || exprBinary.op() == NEQ) {
			if (e0.type() != e1.type()) {
				throw new TypeException(exprBinary.first, "Type mismatch from ExprBinary - Expression0.type != Expression1.type");
			}
			if (hasArgsExpr) {
				throw new TypeException(exprBinary.first, "EQ, NQ operation not allowed with expr args");
			}
			exprBinary.setType(Type.Boolean);
			return exprBinary;
		}
		
		if (exprBinary.op() == LT || exprBinary.op() == GT || exprBinary.op() == LE || exprBinary.op() == GE) {
			if (!(e0.type() == e1.type() && e0.type() == Type.Int)) {
				throw new TypeException(exprBinary.first, "Type mismatch from ExprBinary - Expression0.type != Expression1.type || Expression0.type != Int");
			}
			if (hasArgsExpr) {
				throw new TypeException(exprBinary.first, "LT, GT, LE, GE operation not allowed with expr args");
			}
			exprBinary.setType(Type.Boolean);
			return exprBinary;
		}
		if (e0.type() == e1.type()) {
			
			if (exprBinary.op() == PLUS) {
				if (!(e0.type() == Type.Int || e0.type() == Type.String)) {
					throw new TypeException(exprBinary.first, "Type mismatch from ExprBinary");
				}
			}
			
			else if (exprBinary.op() == MINUS) {
				if (e0.type() != Type.Int) {
					throw new TypeException(exprBinary.first, "Type mismatch from ExprBinary");
				}
			}
			
			exprBinary.setType(e0.type());
			return exprBinary;
		}
		if ((e0.type() == e1.type() && e0.type() == Type.Int)) {
			exprBinary.setType(Type.Int);
			return exprBinary;
		}
		
		throw new TypeException(exprBinary.first, "Type mismatch from ExprBinary");
	}

	@Override
	public Object visitExprConditional(ExprConditional exprConditional, Object arg) throws Exception {
		Expression e0 = (Expression)exprConditional.condition.visit(this, arg);
		Expression e1 = (Expression)exprConditional.trueCase.visit(this, arg);
		Expression e2 = (Expression)exprConditional.falseCase.visit(this, arg);
		
		if (e0.type() != Type.Boolean)
			throw new TypeException(exprConditional.first, "Expression0.type != Boolean");
		
		if (e1.type() != e2.type())
			throw new TypeException(exprConditional.first, "Expression1.type != Expression2.type");
				
		exprConditional.setType(e1.type());;
		
		return exprConditional;
		
	}

	@Override
	public Object visitExprConst(ExprConst exprConst, Object arg) throws Exception {
		exprConst.setType(Type.Int);
		return exprConst;
	}

	@Override
	public Object visitExprHash(ExprHash exprHash, Object arg) throws Exception {
		Expression e = (Expression)exprHash.e.visit(this, arg);
		if (e.type() == Type.Int || e.type() == Type.Image) {
			if (e.type() == Type.Int) {
				if (!(exprHash.attr.equals("red") || exprHash.attr.equals("green") || exprHash.attr.equals("blue"))) {
					throw new TypeException(exprHash.first, "Type mismatch in ExprHash");
				}
			}
			else if (e.type() == Type.Image) {
				if (!(exprHash.attr.equals("width") || exprHash.attr.equals("height"))) {
					throw new TypeException(exprHash.first, "Type mismatch in ExprHash");
				}
			}
			exprHash.setType(Type.Int);;
		}
		
		return exprHash;
	}

	@Override
	public Object visitExprIntLit(ExprIntLit exprIntLit, Object arg) throws Exception {
		exprIntLit.setType(Type.Int);
		return exprIntLit;
	}

	@Override
	public Object visitExprPixelConstructor(ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
		Expression er = (Expression)exprPixelConstructor.redExpr.visit(this, arg);
		Expression eg = (Expression)exprPixelConstructor.greenExpr.visit(this, arg);
		Expression eb = (Expression)exprPixelConstructor.blueExpr.visit(this, arg);
		
		if (er.type() == eg.type() && er.type() == eb.type() && er.type() == Type.Int) {
			exprPixelConstructor.setType(Type.Int);
			return exprPixelConstructor;
		}
		
		throw new TypeException(exprPixelConstructor.first, "Type mismatch in exprPixelConstructor" );
		
	}

	
	@Override
	public Object visitExprPixelSelector(ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
		Expression e = (Expression)exprPixelSelector.image.visit(this, arg);
		Expression ex = (Expression)exprPixelSelector.X.visit(this, arg);
		Expression ey = (Expression)exprPixelSelector.Y.visit(this, arg);
		
		if (e.type() == Type.Image) {
			if (ex.type() == ey.type() && ex.type() == Type.Int) {
				exprPixelSelector.setType(Type.Int);
			}
			return exprPixelSelector;
		}
		
		throw new TypeException(exprPixelSelector.first, "Type mismatch in exprPixelSelector" );
		
	}

	
	@Override
	public Object visitExprStringLit(ExprStringLit exprStringLit, Object arg) throws Exception {
		exprStringLit.setType(Type.String);
		return exprStringLit;
	}

	
	@Override
	public Object visitExprUnary(ExprUnary exprUnary, Object arg) throws Exception {
		Expression e = (Expression)exprUnary.e.visit(this, arg);
		
		if (exprUnary.op() == PLUS || exprUnary.op() == MINUS) {
			if (e.type() != Type.Int) {
				throw new TypeException(exprUnary.first, "Type mismatch in exprUnary" );
			}
			exprUnary.setType(Type.Int);
			return exprUnary;
		}
		
		if (exprUnary.op() == EXCL) {
			if (e.type() != Type.Boolean) {
				throw new TypeException(exprUnary.first, "Type mismatch in exprUnary" );
			}
			exprUnary.setType(Type.Boolean);;
		}
		
		return exprUnary;
		
	}

	@Override
	public Object visitExprVar(ExprVar exprVar, Object arg) throws Exception {
		if (!symbolTable_Dec.containsKey(exprVar.name) && (exprVar.first.kind() == KW_X || exprVar.first.kind() == KW_Y)) {
			//throw new TypeException(exprVar.first, "Type mismatch in exprVar" );
			exprVar.setType(Type.Int);
			Token t_x = new Token(KW_X, 0, 1, 1, 1);
			Token t_y = new Token(KW_Y, 0, 1, 1, 1);
			symbolTable_Dec.put("X", new DecVar(t_x, Type.Int, "X", null));
			symbolTable_Dec.put("Y", new DecVar(t_y, Type.Int, "Y", null));
			return exprVar;
		}
		
		Dec dec = symbolTable_Dec.get(exprVar.name);
		if (dec == null)
			throw new TypeException(exprVar.first, "Expression variable not declared" );
		exprVar.setType(dec.type());
		return exprVar;
		
	}


	/**
	 * First visit method that is called.  It simply visits its children and returns null if no type errors were encountered.  
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for(ASTNode node: program.decOrStatement()) {
			node.visit(this, arg);
		}
		return null;
	}
	

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		if(symbolTable_Dec.containsKey(statementAssign.name)) {
			statementAssign.setDec(symbolTable_Dec.get(statementAssign.name));
		}
		else {
			throw new TypeException(statementAssign.first, "Statement not declared" );
		}
		Dec dec = symbolTable_Dec.get(statementAssign.name);
		Expression expression = (Expression)statementAssign.expression.visit(this, arg);
		
		if (dec.type() != expression.type()) {
			throw new TypeException(statementAssign.first, "IDENT.type == Expression.type");
		}
		
		return statementAssign;
		
	}

	@Override
	public Object visitStatementImageIn(StatementImageIn statementImageIn, Object arg) throws Exception {
		
		if(symbolTable_Dec.containsKey(statementImageIn.name)) {
			statementImageIn.setDec(symbolTable_Dec.get(statementImageIn.name));
		}
		else {
			throw new TypeException(statementImageIn.first, "Statement not declared" );
		}
		
		Dec dec = symbolTable_Dec.get(statementImageIn.name);
		
		if (dec.type() != Type.Image) {
			throw new TypeException(statementImageIn.first, "IDENT.type != Image");
		}
		
		Expression source = (Expression)statementImageIn.source.visit(this, arg);
		if (!(source.type() == Type.Image || source.type() == Type.String))
			throw new TypeException(statementImageIn.first, "Expression.type != Image or String");
			
		return statementImageIn;
		
	}

	@Override
	public Object visitStatementLoop(StatementLoop statementLoop, Object arg) throws Exception {
		if(symbolTable_Dec.containsKey(statementLoop.name)) {
			statementLoop.setDec(symbolTable_Dec.get(statementLoop.name));
		}
		else {
			throw new TypeException(statementLoop.first, "Statement not declared" );
		}
		Dec dec = symbolTable_Dec.get(statementLoop.name);
		
		if (dec.type() != Type.Image) {
			throw new TypeException(statementLoop.first, "IDENT.type != Image");
		}
		
		Expression e0 = (Expression)statementLoop.cond.visit(this, arg);
		Expression e1 = (Expression)statementLoop.e.visit(this, arg);
		
		if (!(e0.type() == Type.Void || e0.type() == Type.Boolean))
			throw new TypeException(statementLoop.first, "Expression0.type != Void or Boolean");
		
		if (e1.type() != Type.Int) 
			throw new TypeException(statementLoop.first, "Expression1.type != Int");
		
		return statementLoop;
	}

	
	@Override
	public Object visitStatementOutFile(StatementOutFile statementOutFile, Object arg) throws Exception {
		if(symbolTable_Dec.containsKey(statementOutFile.name)) {
			statementOutFile.setDec(symbolTable_Dec.get(statementOutFile.name));
		}
		else {
			throw new TypeException(statementOutFile.first, "Statement not declared" );
		}
		
		Dec dec = symbolTable_Dec.get(statementOutFile.name);
		if (dec.type() != Type.Image)
			throw new TypeException(statementOutFile.first, "IDENT is not of type image" );
		Expression e = (Expression)statementOutFile.filename.visit(this, arg);
		if (e.type() != Type.String)
			throw new TypeException(statementOutFile.first, "Expression Type is not String" );
		
		return statementOutFile;
	}

	@Override
	public Object visitStatementOutScreen(StatementOutScreen statementOutScreen, Object arg) throws Exception {
		if(symbolTable_Dec.containsKey(statementOutScreen.name)) {
			statementOutScreen.setDec(symbolTable_Dec.get(statementOutScreen.name));
		}
		else {
			throw new TypeException(statementOutScreen.first, "Statement not declared" );
		}
		Expression x = (Expression)statementOutScreen.X.visit(this, arg);
		Expression y = (Expression)statementOutScreen.Y.visit(this, arg);
		
		if (x.type() != y.type()) {
			throw new TypeException(statementOutScreen.first, "Expression0 type != Expression1 type" );
		}
		Dec dec = symbolTable_Dec.get(statementOutScreen.name);
		if (dec.type() == Type.Int || dec.type() == Type.String) {
			if (x.type() != Type.Void)
				throw new TypeException(statementOutScreen.first, "Expression0.type == Void when (IDENT.type == Int || IDENT.type == String)");
			else
				return statementOutScreen; 
		}
		
		else if (dec.type() == Type.Image) {
			if (!(x.type() == Type.Int || x.type() == Type.Void)) {
				throw new TypeException(statementOutScreen.first, "Expression0.type != Int or Void when IDENT.type == Image" );
			}
			
			else
				return statementOutScreen; 
		}
		
		else 
			throw new TypeException(statementOutScreen.first, "Error in StatementOutScreen" );
		
	}
	
	@Override
	public Object visitExprEmpty(ExprEmpty exprEmpty, Object arg) throws Exception {
		exprEmpty.setType(Type.Void); 
		
		return exprEmpty;
	}

}
