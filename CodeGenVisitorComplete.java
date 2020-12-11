/**
 * This code was developed for the class project in COP5556 Programming Language Principles 
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

import cop5556fa20.AST.Type;
import cop5556fa20.AST.*;
import cop5556fa20.runtime.LoggedIO;
import cop5556fa20.runtime.PLPImage;
import cop5556fa20.runtime.CreatePLPImage;
import cop5556fa20.runtime.PixelOps;
import org.objectweb.asm.*;
import static cop5556fa20.Scanner.Kind.STRINGLIT;
import static cop5556fa20.Scanner.Kind.AND;
import static cop5556fa20.Scanner.Kind.INTLIT;
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
import static cop5556fa20.Scanner.Kind.STAR;
import static cop5556fa20.Scanner.Kind.DIV;
import static cop5556fa20.Scanner.Kind.MOD;

import java.util.List;

public class CodeGenVisitorComplete implements ASTVisitor, Opcodes {

	final String className;
	final boolean isInterface = false;
	ClassWriter cw;
	MethodVisitor mv;

	public CodeGenVisitorComplete(String className) {
		super();
		this.className = className;
	}
	
	
	@Override
	public Object visitDecImage(DecImage decImage, Object arg) throws Exception {
		FieldVisitor fv = cw.visitField(ACC_STATIC, decImage.name(), PLPImage.desc, null, null);
		fv.visitEnd();
		mv.visitFieldInsn(GETSTATIC, "cop5556fa20/Scanner$Kind", decImage.op().toString(), "Lcop5556fa20/Scanner$Kind;");
		if (decImage.width() != Expression.empty) {
			decImage.width().visit(this, null);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		else 
			mv.visitInsn(ACONST_NULL);
		if (decImage.height() != Expression.empty) {
			decImage.height().visit(this, null);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		else 
			mv.visitInsn(ACONST_NULL);
		if (decImage.source() != Expression.empty) 
			decImage.source().visit(this, null);
		else
			mv.visitInsn(ACONST_NULL);
		mv.visitLdcInsn(decImage.first().posInLine());
		mv.visitLdcInsn(decImage.first().line());
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa20/runtime/CreatePLPImage", "create", CreatePLPImage.createSig, false);
		mv.visitFieldInsn(PUTSTATIC, className, decImage.name(), PLPImage.desc);
		return null;
		//throw new UnsupportedOperationException("not yet implemented");
	}


	
	@Override
	public Object visitDecVar(DecVar decVar, Object arg) throws Exception {
		String varName = decVar.name();
		Type type = decVar.type();
		String desc;
		Object value = null;
		if (type == Type.String) {desc = "Ljava/lang/String;";}
		else {
			desc = "I";
			value = Integer.valueOf(0);
		}
		FieldVisitor fieldVisitor = cw.visitField(ACC_STATIC, varName, desc, null, value);
		fieldVisitor.visitEnd();
		Expression e = decVar.expression();
		if (e != Expression.empty) {
			e.visit(this, type); // generates code to evaluate expression and leave value on top of the stack
			mv.visitFieldInsn(PUTSTATIC, className, varName, desc);
		}
//		decVar.slotNumber = this.slotNumber;
//		this.slotNumber++;
		return null;
		//throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExprArg(ExprArg exprArg, Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, 0);
		exprArg.e().visit(this, null);
		mv.visitInsn(AALOAD);
		if (exprArg.type() == Type.Int) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
		}
		return null;
	}

	@Override
	public Object visitExprBinary(ExprBinary exprBinary, Object arg) throws Exception {
		Expression e0 = (Expression) exprBinary.e0();
		Expression e1 = (Expression) exprBinary.e1();
		e0.visit(this, null);
		e1.visit(this, null);
		switch (exprBinary.op()) {
			case PLUS -> {
				if (e0.type() == Type.Int) 
					mv.visitInsn(IADD);
				else 
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", isInterface);
			}
			case MINUS -> mv.visitInsn(ISUB);
			case STAR -> mv.visitInsn(IMUL);
			case DIV -> mv.visitInsn(IDIV);
			case MOD -> mv.visitInsn(IREM);
			
			case AND -> mv.visitInsn(IAND);
			case OR -> mv.visitInsn(IOR);
			
			case LT -> {
				Label l1 = new Label();
				Label l2 = new Label();
				mv.visitJumpInsn(IF_ICMPLT, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			case GT -> {
				Label l1 = new Label();
				Label l2 = new Label();
				mv.visitJumpInsn(IF_ICMPGT, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			
			case LE -> {
				Label l1 = new Label();
				Label l2 = new Label();
				mv.visitJumpInsn(IF_ICMPLE, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			case GE -> {
				Label l1 = new Label();
				Label l2 = new Label();
				mv.visitJumpInsn(IF_ICMPGE, l1);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(l2);
			}
			
			case EQ -> {
				if (e0.type() == Type.Int) {
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitJumpInsn(IF_ICMPEQ, l1);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
				} else {
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitJumpInsn(IF_ACMPEQ, l1);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
				}
			}
			case NEQ -> {
				if (e0.type() == Type.Int) {
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitJumpInsn(IF_ICMPNE, l1);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
				} else {
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitJumpInsn(IF_ACMPNE, l1);
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(l2);
				}
			}
			default -> throw new UnsupportedOperationException("Invalid operator");
		}
		return null;
	}

	@Override
	public Object visitExprConditional(ExprConditional exprConditional, Object arg) throws Exception {
		Expression condition = exprConditional.condition();
		condition.visit(this, null);
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		Expression trueCase = exprConditional.trueCase();
		trueCase.visit(this, null);
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		Expression falseCase = exprConditional.falseCase();
		falseCase.visit(this, null);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitExprConst(ExprConst exprConst, Object arg) throws Exception {
		mv.visitLdcInsn(exprConst.value());
		return null;
	}

	@Override
	public Object visitExprHash(ExprHash exprHash, Object arg) throws Exception {
		Expression e = exprHash.e();
		e.visit(this, null);
		if (e.type() == Type.Image) {
			int line = exprHash.first().line();
			int posInLine = exprHash.first().posInLine();
			mv.visitLdcInsn(line);
			mv.visitLdcInsn(posInLine);
			if (exprHash.attr().equals("width")) 
				mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);
			else if (exprHash.attr().equals("height"))
				mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);
		}
		if (e.type() == Type.Int) {
			if (exprHash.attr().equals("blue")) 
				mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "getBlue", PixelOps.getBlueSig, false);
			if (exprHash.attr().equals("green")) 
				mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "getGreen", PixelOps.getGreenSig, false);
			if (exprHash.attr().equals("red")) 
				mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "getRed", PixelOps.getRedSig, false);
		}
		return null;
	}

	@Override
	public Object visitExprIntLit(ExprIntLit exprIntLit, Object arg) throws Exception {
		mv.visitLdcInsn(exprIntLit.value());
		return null;
	}

	@Override
	public Object visitExprPixelConstructor(ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
		Expression redExpr = exprPixelConstructor.redExpr();
		Expression greenExpr = exprPixelConstructor.greenExpr();
		Expression blueExpr = exprPixelConstructor.blueExpr();
		redExpr.visit(this, null);
		greenExpr.visit(this, null);
		blueExpr.visit(this, null);
		mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "makePixel", PixelOps.makePixelSig, false);
		return null;
	}

	@Override
	public Object visitExprPixelSelector(ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
		exprPixelSelector.image().visit(this, null);
		exprPixelSelector.X().visit(this, null);
		exprPixelSelector.Y().visit(this, null);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "selectPixel", PLPImage.selectPixelSig, false);
		return null;
	}
	
	/**
	 * generate code to put the value of the StringLit on the stack.
	 */
	@Override
	public Object visitExprStringLit(ExprStringLit exprStringLit, Object arg) throws Exception {
		mv.visitLdcInsn(exprStringLit.text());
		return null;
	}

	@Override
	public Object visitExprUnary(ExprUnary exprUnary, Object arg) throws Exception {
		Expression e = exprUnary.e();
		if (exprUnary.op().equals(EXCL)) {
			e.visit(this, null);
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitJumpInsn(IFEQ, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, l2);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(l2);
		}
		if (exprUnary.op().equals(PLUS) || exprUnary.op().equals(MINUS)){
			e.visit(this, Type.Int);
			if (exprUnary.op().equals(MINUS)) {
				mv.visitInsn(INEG);
			}
		}
		return null;
	}

	@Override
	public Object visitExprVar(ExprVar exprVar, Object arg) throws Exception {
		String varName = exprVar.name();
		if (exprVar.type() == Type.Int) {
			if (varName.equals("X")) {
				mv.visitVarInsn(ILOAD, 1);
			} else if (varName.equals("Y")) {
				mv.visitVarInsn(ILOAD, 2);
			} else {
				mv.visitFieldInsn(GETSTATIC, className, varName, "I");
			}
		} 
		else if (exprVar.type() == Type.String)
			mv.visitFieldInsn(GETSTATIC, className, varName, "Ljava/lang/String;");
		
		else if (exprVar.type() == Type.Image)
			mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);

		return null;
	}
	
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);


		int version = -65478;
		cw.visit(version, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(null, null);
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, new String[] { "java/lang/Exception" });
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		List<ASTNode> nodes = program.decOrStatement();
		for (ASTNode node : nodes) {
			node.visit(this, null);
		}
		mv.visitInsn(RETURN);

		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("width", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("height", "I", null, mainStart, mainEnd, 4);

		
		mv.visitMaxs(0, 0);

		mv.visitEnd();

		cw.visitEnd();

		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		Dec dec = statementAssign.dec();
		String varName = statementAssign.name();
		statementAssign.expression().visit(this, null);
		Type  type = Type.Void;
		if (dec.type() == Type.Int) {
			mv.visitFieldInsn(PUTSTATIC, className, varName, "I");
		}
		else if (dec.type() == Type.String)
			mv.visitFieldInsn(PUTSTATIC, className, varName, "Ljava/lang/String;");
		else if (dec.type() == Type.Image) {
			mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);
			statementAssign.expression().visit(this, null);
			mv.visitLdcInsn(statementAssign.first().posInLine());
			mv.visitLdcInsn(statementAssign.first().line());
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa20/runtime/CreatePLPImage", "assign", CreatePLPImage.assignSig, false);
		}
		return null;
	}

	@Override
	public Object visitStatementImageIn(StatementImageIn statementImageIn, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, statementImageIn.name(), PLPImage.desc);
		statementImageIn.source().visit(this, null);
		mv.visitMethodInsn(INVOKESTATIC, "cop5556fa20/runtime/CreatePLPImage", "copy", CreatePLPImage.copySig, false);
		
		return null;
	}

	@Override
	public Object visitStatementLoop(StatementLoop statementLoop, Object arg) throws Exception {

		String varName = statementLoop.name();
		int line = statementLoop.first().line();
		int posInLine = statementLoop.first().posInLine();
		Expression e = statementLoop.e();
		
		mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);
		mv.visitLdcInsn(line);
		mv.visitLdcInsn(posInLine);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "ensureImageAllocated", PLPImage.ensureImageAllocatedSig, isInterface);
		mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);
		mv.visitLdcInsn(line);
		mv.visitLdcInsn(posInLine);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, isInterface);
		mv.visitVarInsn(ISTORE, 3);
		mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);
		mv.visitLdcInsn(line);
		mv.visitLdcInsn(posInLine);
		mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, isInterface);
		mv.visitVarInsn(ISTORE, 4);

		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		Label l4 = new Label();
			
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 1);
		mv.visitLabel(l1);	
		mv.visitVarInsn(ILOAD, 1);
		mv.visitVarInsn(ILOAD, 3);
		mv.visitJumpInsn(IF_ICMPGE, l2);		
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 2);			
		mv.visitLabel(l3);			
		mv.visitVarInsn(ILOAD, 2);
		mv.visitVarInsn(ILOAD, 4);
		mv.visitJumpInsn(IF_ICMPGE, l4);				
		if (statementLoop.cond() == Expression.empty) {
			mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			e.visit(this, null);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updatePixel", PLPImage.updatePixelSig, isInterface);
		}
		else {
			Label label1 = new Label();
			statementLoop.cond().visit(this, null);
			mv.visitJumpInsn(IFEQ, label1);
			mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			e.visit(this, null);
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updatePixel", PLPImage.updatePixelSig, isInterface);
			mv.visitLabel(label1);
		}
		mv.visitInsn(ICONST_1);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitInsn(IADD);
		mv.visitVarInsn(ISTORE, 2);
		mv.visitJumpInsn(GOTO, l3);
		mv.visitLabel(l4);	
		mv.visitInsn(ICONST_1);
		mv.visitVarInsn(ILOAD, 1);
		mv.visitInsn(IADD);
		mv.visitVarInsn(ISTORE, 1);
		mv.visitJumpInsn(GOTO, l1);
		mv.visitLabel(l2);

		return null;
	}

	@Override
	public Object visitExprEmpty(ExprEmpty exprEmpty, Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitStatementOutFile(StatementOutFile statementOutFile, Object arg) throws Exception {
		Expression filename = statementOutFile.filename();
		String varName = statementOutFile.name();
		mv.visitFieldInsn(GETSTATIC, className, varName, PLPImage.desc);
		filename.visit(this, null);
		mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "imageToFile", LoggedIO.imageToFileSig, isInterface);
		return null;
	}

	@Override
	public Object visitStatementOutScreen(StatementOutScreen statementOutScreen, Object arg) throws Exception {
		String name = statementOutScreen.name();
		
		Type type = statementOutScreen.dec().type();
		if (type == Type.String) {
			mv.visitFieldInsn(GETSTATIC, className, name, "Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "stringToScreen", LoggedIO.stringToScreenSig, isInterface);
		}
		else if (type == Type.Int) {
			mv.visitFieldInsn(GETSTATIC, className, name, "I");
			mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "intToScreen", LoggedIO.intToScreenSig, isInterface);
		}
		else if (type == Type.Image) {
			Expression X = statementOutScreen.X();
			Expression Y = statementOutScreen.Y();
			mv.visitFieldInsn(GETSTATIC, className, statementOutScreen.name(), PLPImage.desc);
			if (statementOutScreen.X() != Expression.empty) {
				X.visit(this, null);
			} else {
				mv.visitInsn(ICONST_0);
			}
			if (Y != Expression.empty) {
				Y.visit(this, null);
			} else {
				mv.visitInsn(ICONST_0);
			}
			mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "imageToScreen", LoggedIO.imageToScreenSig, isInterface);
		}
		return null;
	}

}
