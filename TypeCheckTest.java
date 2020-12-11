package cop5556fa20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cop5556fa20.AST.ASTNode;
import cop5556fa20.AST.Program;
import cop5556fa20.Parser.SyntaxException;
import cop5556fa20.Scanner.LexicalException;
import cop5556fa20.TypeCheckVisitor.TypeException;

@SuppressWarnings("preview")
class TypeCheckTest {

	/*
	 * To make it easy to print objects and turn this output on and off.
	 */
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}
	
	/**
	 * Test input program that is expected to be syntactically correct and pass type checking.
	 * @param input   
	 * @throws Exception
	 */
	void pass(String input) throws Exception {
		Program prog = parseAndGetProgram(input);		
		TypeCheckVisitor v = new TypeCheckVisitor();
		prog.visit(v, null);
		show(prog);
	}
	
	
	/**
	 * Test input program that is expected to be syntactically correct, but fails type checking.
	 * @param input
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	void fail(String input) throws LexicalException, SyntaxException {
		Program prog = parseAndGetProgram(input);
		//show(prog);  //Display the AST
		TypeCheckVisitor v = new TypeCheckVisitor();
		Exception exception = assertThrows(TypeException.class, () -> {
			prog.visit(v, null);
		});
		show(exception);
	}
	
	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		//show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	
	Program parseAndGetProgram(String input) throws LexicalException, SyntaxException{
		Parser parser = makeParser(input);
		ASTNode node = parser.parse();
		assertFalse(parser.scanner.hasTokens());
		return (Program)node;
	}
	
	@Test
	public void testEmpty() throws Exception {
		String input = "";  //The input is the empty string.  This is legal
		Program prog = parseAndGetProgram(input);
		TypeCheckVisitor v = new TypeCheckVisitor();
		prog.visit(v, null);
	}	
	
	
	/**
	 * This is one of the simplest nonempty correct programs.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testdec0() throws Exception {
		String input = """
				int x;
				""";  
		pass(input);
	}	
		

	/**
	 * This program fails type checking due to the attempt to redefine x
	 * @throws Exception
	 */
	@Test
	public void testdec1_fail() throws Exception {
		String input = """
				int x;
				string x;
				""";  
		fail(input);
	}
	
	
	@Test
	 public void test1() throws Exception {
		String input = """
				int abc;
				string bcd = "a";
				""";
	 
	 pass(input);
	 }
	
	@Test
	 public void test2() throws Exception {
		String input = """
				image abc;
				abc <- "https://this.is.a.url";
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test3() throws Exception {
		String input = """
				int abc = 4 + RED;
				""";
	 
		pass(input);
	 }

	@Test
	 public void test4() throws Exception {
		String input = """
				int abc = 4;
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test5() throws Exception {
		String input = """
				image abc;
				abc -> screen[10, 40];
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test6() throws Exception {
		String input = """
				image abc;
				int x = @ abc [1000, 2000];
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test7() throws Exception {
		String input = """
				image abc;
				int x = @ abc [1000, 2000] # green;
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test8() throws Exception {
		String input = """
				image abc;
				int a;
				int b;
				abc =* [X,Y] : ( a < b ) : a * + b;
				""";
	 
		pass(input);
	 }
	
	
	
	@Test
	 public void test10() throws Exception {
		String input = """
				image im2;
				image[1000,2000]  im= im2;
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test11() throws Exception {
		String input = """
				image input;
				input -> "Why am I doing this?";
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test12() throws Exception {
		String input = """
				int input;
				input = a * + b;
				string input;
				input = "value";
				""";
	 
		fail(input);
	 }
	
	@Test
	 public void test13() throws Exception {
		String input = """
				string b = @1;
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test14() throws Exception {
		String input = """
				int a = @0 + @1;
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test15() throws Exception {
		String input = """
				int a = ((@0 + @1) < 10) ? 1 : 0;
				""";
	 
		fail(input);
	 }
	
	@Test
	 public void test16() throws Exception {
		String input = """
				int a = (@0 == @1 ? 0 : 1);
				""";
	 
		fail(input);
	 }
	
	@Test
	 public void test17() throws Exception {
		String input = """
				int a = (@1 + @2) == 3;
				""";
	 
		fail(input);
	 }
	
	@Test
	 public void test18() throws Exception {
		String input = """
				int a = @@0;
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test19() throws Exception {
		String input = """
				int a = (1 == 1) ? @0 : @1;
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test20() throws Exception {
		String input = """
				int h;\nimage[w,h] a;\n
				""";
	 
		fail(input);
	 }
	
	@Test
	 public void test21() throws Exception {
		String input = """
				image x;\nx <- \"filename\";\nx <- @0;\n
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test22() throws Exception {
		String input = """
				image x;\nint p;\nint w = x#width;\nint h = x#height;\nint r = p#red;\nint g = p#green;\nint b = p#blue;\n
				""";
	 
		pass(input);
	 }
	@Test
	 public void test23() throws Exception {
		String input = """
				image x <- @0;\nimage [x#height, x#width]  y;\ny = *[X,Y]: X<Y: <<x[X,Y]#red,x[X,Y]#green, x[X,Y]#blue>>;\n
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test24() throws Exception {
		String input = """
				image x <- @0;\nimage [x#height, x#width]  y;\ny = *[X,Y]:: <<0, X<Y?GREEN:BLUE, 0>>;\n
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test25() throws Exception {
		String input = """
				image x <- @0;\nimage[x#height, x#width] y;\ny = *[X,Y]:: x[Y,X];\n
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test26() throws Exception {
		String input = """
				string x;\nx = 2 < 3  ? @0 : @1;\n
				""";
	 
		pass(input);
	 }
	
	@Test
	 public void test27() throws Exception {
		String input = """
				int x;\nx = 2 < 3  ? @0 : @1;\n
				""";
	 
		pass(input);
	 }
}