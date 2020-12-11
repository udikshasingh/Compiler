/**
 * Code for the class project in COP5556 Programming Language Principles 
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

package cop5556fa20.AST;

import cop5556fa20.Scanner.Token;
import static cop5556fa20.Scanner.Kind;

public class ExprUnary extends Expression {
	
	final Kind op;
	public final Expression e;
	public Type type;
	
	public ExprUnary(Token first, Kind op, Expression e) {
		super(first);
		this.op = op;
		this.e = e;
	}


	public Kind op() {
		return op;
	}
	public Expression e() {
		return e;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((e == null) ? 0 : e.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExprUnary other = (ExprUnary) obj;
		if (e == null) {
			if (other.e != null)
				return false;
		} else if (!e.equals(other.e))
			return false;
		if (op == null) {
			if (other.op != null)
				return false;
		} else if (!op.equals(other.op))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ExprUnary [op=" + op + ", e=" + e + "]";
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitExprUnary(this,arg);
	}
	
	

}
