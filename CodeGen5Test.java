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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import cop5556fa20.AST.Program;
import cop5556fa20.CodeGenUtils.DynamicClassLoader;
import cop5556fa20.runtime.LoggedIO;

class CodeGen5Test {

	static boolean doPrint = true;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}
	
	/**
	 * Generates and returns byte[] containing classfile implmenting given input program.
	 * 
	 * Throws exceptions for Lexical, Syntax, and Type checking errors
	 * 
	 * @param input   		String containing source code
	 * @param className		className and fileName of generated code
	 * @return        		Generated bytecode
	 * @throws Exception
	 */
	byte[] genCode(String input, String className, boolean doCreateFile) throws Exception {
		show(input);
		//scan, parse, and type check
		Scanner scanner = new Scanner(input);
		
		scanner.scan();
		Parser parser = new Parser(scanner);
		Program program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, className);
		show(program);

		//generate code
		CodeGenVisitorComplete cv = new CodeGenVisitorComplete(className);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		//output the generated bytecode
		show(CodeGenUtils.bytecodeToString(bytecode));
		
		//write byte code to file 
		if (doCreateFile) {
			String classFileName = "bin/" + className + ".class";
			OutputStream output = new FileOutputStream(classFileName);
			output.write(bytecode);
			output.close();
			System.out.println("wrote classfile to " + classFileName);
		}
		
		//return generated classfile as byte array
		return bytecode;
	}
	
	/**
	 * Dynamically loads and executes the main method defined in the provided bytecode.
	 * If there are no command line arguments, commandLineArgs shoudl be an empty string (not null).
	 * 
	 * @param className
	 * @param bytecode
	 * @param commandLineArgs
	 * @throws Exception
	 */
	void runCode(String className, byte[] bytecode, String[] commandLineArgs) throws Exception  {
		LoggedIO.clearGlobalLog(); //initialize log used for testing.
		DynamicClassLoader loader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
		Class<?> testClass = loader.define(className, bytecode);
		@SuppressWarnings("rawtypes")
		Class[] argTypes = {commandLineArgs.getClass()};
		Method m = testClass.getMethod("main", argTypes );
		show("Command line args: " + Arrays.toString(commandLineArgs));
		show("Output from " + m + ":");  //print name of method to be executed
		Object passedArgs[] = {commandLineArgs};  //create array containing params, in this case a single array.
		try {
		m.invoke(null, passedArgs);	
		}
		catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof Exception) {
				Exception ec = (Exception) e.getCause();
				throw ec;
			}
			throw  e;
		}
	}
	
	/** Hello, World
	 */
	@Test
	public void helloWorld() throws Exception {
		String className = "HelloWorld";
		String input = """
				string s = "Hello, World!";
				s -> screen;
				""";
		byte[] bytecode = genCode(input, className, false);
		String[] args = {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("Hello, World!");
		assertEquals(expectedLog, LoggedIO.globalLog);
	}
	
	@Test
	public void commandLineArg0() throws Exception {
		String className = "CommandLineArg0";
		String input = """
				string s;
				s = @0;
				s -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {"Hello from the command line!"};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(args[0]);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	

	@Test
	public void test1() throws Exception {
		String className = "TestDecVarString";
		String input = """
				string s;
				s -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(null);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	
	@Test
	public void test2() throws Exception {
		String className = "TestDecVarInt";
		String input = """
				int a;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(0);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test3() throws Exception {
		String className = "TestDecVarIntWithValue";
		String input = """
				int a = 10;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(10);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test4() throws Exception {
		String className = "TestImageOutStatementString";
		String input = """
				string a = "Statement printed on screen";
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("Statement printed on screen");
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test5() throws Exception {
		String className = "TestImageOutStatementInt";
		String input = """
				int a = 1000;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(1000);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test6() throws Exception {
		String className = "TestStatementAssignInt";
		String input = """
				int a = 1000 + 1000;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2000);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test7() throws Exception {
		String className = "TestStatementAssignString";
		String input = """
				string a = "String is ";
				string b = "concatenated";
				string c = a + b;
				c -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("String is concatenated");
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test8() throws Exception {
		String className = "TestStatementAssignString";
		String input = """
				int a = 10 % 10;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(0);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test9() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = !(2 == 2) ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test10() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = !(2 < 3) ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test11() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = !(2 != 3) ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	@Test
	public void test12() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = !(2 <= 3) ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	@Test
	public void test13() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = !(2 >= 3) ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(1);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	@Test
	public void test14() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = (2 == 3) ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	@Test
	public void test15() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 2 != 3 ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(1);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	@Test
	public void test16() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 2 > 3 ? 1 : 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test17() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 2 + 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(4);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	@Test
	public void test18() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				string s1 = "udiksha ";
				string s2 = "singh";
				string a = s1 + s2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("udiksha singh");
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test19() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 2 - 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(0);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test20() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 2 * 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(4);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test21() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 2 / 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(1);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test22() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 2 % 2;
				a -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(0);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test23() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = 20;
				int b = -a;
				b -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(-20);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test24() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = @0;
				int b = @1;
				int c = a + b;
				c -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {"10", "20"};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(30);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test25() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int a = @0;
				int b = @1;
				int c = a - b;
				c -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {"10", "20"};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(-10);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test26() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				string a = @0;
				string b = @1;
				string c = a + b;
				c -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {"udiksha ", "singh"};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("udiksha singh");
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test27() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				
				string s3 = ("udiksha" == "udiksha") ? "equal" : "not equal";
				s3 -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("equal");
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test28() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				
				string s3 = ("udiksha" != "udiksha") ? "equal" : "not equal";
				s3 -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("not equal");
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test29() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				
				string s3 = ("udiksha" != "udiksha") & ("udi" == "udi") ? "equal" : "not equal";
				s3 -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("not equal");
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test30() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				
				string s3 = ("udiksha" != "udiksha") | ("udi" == "udi") ? "equal" : "not equal";
				s3 -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add("equal");
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test31() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				
				int s3 = (2 != 3) | (4 == 4) ? 1 : 0;
				s3 -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(1);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test32() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int s3 = (2 != 3) & (4 != 4) ? 1 : 0;
				s3 -> screen;
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(0);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test33() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int c = 4 <= 4 ? 1 : 2;\nc -> screen;\n
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(1);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test34() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int c = 4 >= 4 ? 1 : 2;\nc -> screen;\n
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(1);
		assertEquals(expectedLog, LoggedIO.globalLog);		
	}
	
	@Test
	public void test35() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int c = 2<3 & 4>4 ? 1 : 2;\nc -> screen;\n
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
	
	@Test
	public void test36() throws Exception {
		String className = "TestConditionalExpression";
		String input = """
				int c = 2<3 & 4>4 ? 1 : 2;\nc -> screen;\n
				""";
				
		byte[] bytecode = genCode(input, className, false);
		String[] args =  {};
		runCode(className, bytecode, args);
		//set up expected log
		ArrayList<Object> expectedLog = new ArrayList<Object>();
		expectedLog.add(2);
		assertEquals(expectedLog, LoggedIO.globalLog);		
		
	}
}