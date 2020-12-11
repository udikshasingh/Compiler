/**
 * Scanner for the class project in COP5556 Programming Language Principles 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Scanner {
	
	@SuppressWarnings("preview")
	public record Token(
		Kind kind,
		int pos, //position in char array.  Starts at zero
		int length, //number of chars in token
		int line, //line number of token in source.  Starts at 1
		int posInLine //position in line of source.  Starts at 1
		) {
	}
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		int pos;
		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		public int pos() { return pos; }
	}
	
	
	public static enum Kind {
		IDENT, INTLIT, STRINGLIT, CONST,
		KW_X/* X */,  KW_Y/* Y */, KW_WIDTH/* width */,KW_HEIGHT/* height */, 
		KW_SCREEN/* screen */, KW_SCREEN_WIDTH /* screen_width */, KW_SCREEN_HEIGHT /*screen_height */,
		KW_image/* image */, KW_int/* int */, KW_string /* string */,
		KW_RED /* red */,  KW_GREEN /* green */, KW_BLUE /* blue */,
		ASSIGN/* = */, GT/* > */, LT/* < */, 
		EXCL/* ! */, Q/* ? */, COLON/* : */, EQ/* == */, NEQ/* != */, GE/* >= */, LE/* <= */, 
		AND/* & */, OR/* | */, PLUS/* + */, MINUS/* - */, STAR/* * */, DIV/* / */, MOD/* % */, 
	    AT/* @ */, HASH /* # */, RARROW/* -> */, LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, LPIXEL /* << */, RPIXEL /* >> */,  SEMI/* ; */, COMMA/* , */,  EOF,
		NOP /* No Operation */
	}
	

	/**
	 * Returns the text of the token.  If the token represents a String literal, then
	 * the returned text omits the delimiting double quotes and replaces escape sequences with
	 * the represented character.
	 * 
	 * @param token
	 * @return
	 */
	public String getText(Token token) {
		if (token.kind == Kind.STRINGLIT) {
			String text = String.valueOf(Arrays.copyOfRange(chars, token.pos + 1, token.pos + token.length - 1));
			if (text.contains("\\n")) {
				int index = text.indexOf("\\n");
				StringBuilder sbr = new StringBuilder(text.substring(0, index));
				sbr.append('\n');
				sbr.append(text.substring(index + 2));
				text = sbr.toString();
			}
			return text;
		}
		String text = String.valueOf(Arrays.copyOfRange(chars, token.pos, token.pos + token.length));
		return text;
	}
	
	
	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}
	
	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	

	/**
	 * The list of tokens created by the scan method.
	 */
	private final ArrayList<Token> tokens = new ArrayList<Token>();
	
	private final char[] chars;
	static final char EOFchar = 0;
	
	private enum State {START, HAVE_EQUAL, DIGITS, STRING, IDENT_PART, COMMENT};

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1);
		
		chars[numChars] = EOFchar;
		
	}
	

	
	public Scanner scan() throws LexicalException {
		
		try {
			String str = String.valueOf(chars);
			str.translateEscapes();
		}
		catch (IllegalArgumentException e) {
			throw new LexicalException(" Illegal character in the Input : " , -1);
		}
		
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		State state = State.START;
		int startPos = 0;
		
		while(pos < chars.length) {
			char ch = chars[pos];
			switch(state) {
			
			case START -> {
				
				startPos = pos;
				switch(ch) {
				
				case '\t',' ' -> {
					pos++;
					posInLine++;
				}
				case '\n' -> {
					pos++;
					line++;
					posInLine = 1;
				}
				case '\r' -> {
					pos++;
					posInLine = 1;
					line++;
					if (chars[pos] == '\n') {
						pos++;
					}
				}
				
				case '\\' -> {
					throw new LexicalException(line + " : " + posInLine + " Illegal String Input : " + (int)chars[pos] , pos);
				}
				
				case '?' -> {
					tokens.add(new Token(Kind.Q, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				
				case '0' -> {
					tokens.add(new Token(Kind.INTLIT, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '+' -> {
					tokens.add(new Token(Kind.PLUS, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '*' -> {
					tokens.add(new Token(Kind.STAR, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '-' -> {
					if (chars[pos + 1] == '>') {
						tokens.add(new Token(Kind.RARROW, startPos, 2, line, posInLine));
						pos += 2;
						posInLine += 2;
					}
					else {
						tokens.add(new Token(Kind.MINUS, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
					}
				}
				case '|' -> {
					tokens.add(new Token(Kind.OR, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '&' -> {
					tokens.add(new Token(Kind.AND, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '=' -> {
					pos++;
					posInLine++;
					state = State.HAVE_EQUAL;
				}
				
				case '1','2','3','4','5','6','7','8','9' -> {
					pos++;
					posInLine++; 
					state= State.DIGITS;
				}
				case '/' -> {
					
					if (chars[pos + 1] == '/') {
						state = State.COMMENT;
						pos += 2;
						posInLine += 2;
					}
					else {
						tokens.add(new Token(Kind.DIV, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
					}
				}
				case ',' -> {
					tokens.add(new Token(Kind.COMMA, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case ';' -> {
					tokens.add(new Token(Kind.SEMI, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case ':' -> {
					tokens.add(new Token(Kind.COLON, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '>' -> {
					if (chars[pos + 1] == '>') {
						tokens.add(new Token(Kind.RPIXEL, startPos, 2, line, posInLine));
						pos += 2;
						posInLine += 2;
					}
					else if (chars[pos + 1] == '=') {
						tokens.add(new Token(Kind.GE, startPos, 2, line, posInLine));
						pos += 2;
						posInLine += 2;
					}
					else {
						tokens.add(new Token(Kind.GT, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
					}
				}
				case '<' -> {
					if (chars[pos + 1] == '<') {
						tokens.add(new Token(Kind.LPIXEL, startPos, 2, line, posInLine));
						pos += 2;
						posInLine += 2;
					}
					else if (chars[pos + 1] == '-') {
						tokens.add(new Token(Kind.LARROW, startPos, 2, line, posInLine));
						pos += 2;
						posInLine += 2;
					}
					else if (chars[pos + 1] == '=') {
						tokens.add(new Token(Kind.LE, startPos, 2, line, posInLine));
						pos += 2;
						posInLine += 2;
					}
					else {
						tokens.add(new Token(Kind.LT, startPos, 1, line, posInLine));
						pos++;
						posInLine++;
					}
				}
				case '[' -> {
					tokens.add(new Token(Kind.LSQUARE, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case ']' -> {
					tokens.add(new Token(Kind.RSQUARE, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '(' -> {
					tokens.add(new Token(Kind.LPAREN, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case ')' -> {
					tokens.add(new Token(Kind.RPAREN, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '#' -> {
					tokens.add(new Token(Kind.HASH, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '@' -> {
					tokens.add(new Token(Kind.AT, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '%' -> {
					tokens.add(new Token(Kind.MOD, startPos, 1, line, posInLine));
					pos++;
					posInLine++;
				}
				case '!' -> {
					if (chars[pos + 1] == '=') {
						tokens.add(new Token(Kind.NEQ, startPos, 2, line, posInLine));
						pos += 2;
						posInLine += 2;
					}
					else {
						tokens.add(new Token(Kind.EXCL, startPos, 1, line, posInLine));
						pos++;
						posInLine++;	
					}
				}
				
				case '"' -> {
					pos++;
					posInLine++;
					state = State.STRING;
				}
				default -> {
					if (Character.isJavaIdentifierStart(ch)) {
						pos++;
						//posInLine++;
						state = State.IDENT_PART;
					}
					else {
						if (ch != EOFchar) {
							//throw new LexicalException(line + " : " + posInLine + " Illegal character " + (int)ch , pos);
							throw new LexicalException(line + " : " + posInLine + " Illegal String Input : " + (int)chars[pos] , pos);
						}
						pos++;
					}
				}
				
				}
				
			}
			
			case HAVE_EQUAL -> {
				if(ch == '=') {
					tokens.add(new Token(Kind.EQ, startPos, 2, line, posInLine - 1));
					pos++;
					posInLine++;
				}
				else {
					tokens.add(new Token(Kind.ASSIGN, startPos, 1, line, posInLine - 1));
				}
				state = State.START;
			}
			
			case DIGITS -> {
				switch (ch) {
				case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
				    pos++;
				    posInLine++;
				    long num = Long.parseLong(String.valueOf(Arrays.copyOfRange(chars, startPos, pos)));
				    if (num > (long)(Integer.MAX_VALUE))
						throw new LexicalException(line + " : " + posInLine + " Int Range  : " + (int)chars[pos] , pos);
				  }
				default -> {
							tokens.add(new Token(Kind.INTLIT, startPos, pos - startPos, line, posInLine - (pos - startPos)));
							state = State.START;
							//posInLine += (pos - startPos);
				  }
				}
			} 
			
			case STRING -> {
				
				while(pos < chars.length - 1 && chars[pos] != '"') {
					if(chars[pos] == '\n' || chars[pos] == '\r')
						throw new LexicalException(line + " : " + posInLine + "CR or LF found in String" + (int)chars[pos] , pos);
					pos++;
					posInLine++;
				}
				
				if(chars[pos] == '"') {
					tokens.add(new Token(Kind.STRINGLIT, startPos, pos - startPos + 1, line, posInLine - (pos - startPos + 1) + 1));
					pos++;
					posInLine++;
					state = State.START;
				}
				else {
					throw new LexicalException(line + " : " + posInLine + " Illegal String Input : " + (int)chars[pos] , pos);
				}
			}
			
			case IDENT_PART -> {
				while(Character.isJavaIdentifierStart(chars[pos]) || Character.isDigit(chars[pos])) {
						pos++;
						//posInLine++;
				}					
					String word = String.valueOf(Arrays.copyOfRange(chars, startPos, pos));
					if (constants.containsKey(word)) {
						tokens.add(new Token(Kind.CONST, startPos, pos - startPos, line, posInLine));
						state = State.START;
					}
					else if (keywords.contains(word)) {
						switch (word) {
						case "X" -> {
							tokens.add(new Token(Kind.KW_X, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "Y" -> {
							tokens.add(new Token(Kind.KW_Y, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "width" -> {
							tokens.add(new Token(Kind.KW_WIDTH, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "height" -> {
							tokens.add(new Token(Kind.KW_HEIGHT, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "screen" -> {
							tokens.add(new Token(Kind.KW_SCREEN, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "screen_width" -> {
							tokens.add(new Token(Kind.KW_SCREEN_WIDTH, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "screen height" -> {
							tokens.add(new Token(Kind.KW_SCREEN_HEIGHT, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "image" -> {
							tokens.add(new Token(Kind.KW_image, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "int" -> {
							tokens.add(new Token(Kind.KW_int, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "string" -> {
							tokens.add(new Token(Kind.KW_string, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "red" -> {
							tokens.add(new Token(Kind.KW_RED, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "green" -> {
							tokens.add(new Token(Kind.KW_GREEN, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						case "blue" -> {
							tokens.add(new Token(Kind.KW_BLUE, startPos, pos - startPos, line, posInLine));
							state = State.START;
						}
						}
					}
					else {
						tokens.add(new Token(Kind.IDENT, startPos, pos - startPos, line, posInLine));
						state = State.START;	
					}
					posInLine += pos - startPos;
			}
			
			case COMMENT -> {
				int len = chars.length;
				while (pos < len - 1 && chars[pos] != '\r' && chars[pos] != '\n') {
					char chr = chars[pos];
					pos++;
					posInLine++;
					
				}
				if (chars[pos] == '\n') {
					pos++;
					line++;
					posInLine = 1;
				}
				else if (chars[pos] == '\r') {
					pos++;
					line++;
					posInLine = 1;
					if(chars[pos] == '\n') {
						pos++;
					}
				}
				state = State.START;
			}
			
			}
		}
		
		tokens.add(new Token(Kind.EOF, pos - 1, 0, line, posInLine));
		return this;
	}
	

	/**
	 * precondition:  This Token is an INTLIT or CONST
	 * @throws LexicalException 
	 * 
	 * @returns the integer value represented by the token
	 */
	public int intVal(Token t) throws LexicalException {
		if(t.kind == Kind.CONST) {
			return constants.get(String.valueOf(Arrays.copyOfRange(chars, t.pos, t.pos + t.length)));
		}
		return Integer.parseInt(String.valueOf(Arrays.copyOfRange(chars, t.pos, t.pos + t.length)));
	}
	
	/**
	 * Hashmap containing the values of the predefined colors.
	 * Included for your convenience.  
	 * 
	 */
	protected static HashMap<String, Integer> constants;
	private static HashSet<String> keywords;
	static {
		constants = new HashMap<String, Integer>();	
		constants.put("Z", 255);
		constants.put("WHITE", 0xffffffff);
		constants.put("SILVER", 0xffc0c0c0);
		constants.put("GRAY", 0xff808080);
		constants.put("BLACK", 0xff000000);
		constants.put("RED", 0xffff0000);
		constants.put("MAROON", 0xff800000);
		constants.put("YELLOW", 0xffffff00);
		constants.put("OLIVE", 0xff808000);
		constants.put("LIME", 0xff00ff00);
		constants.put("GREEN", 0xff008000);
		constants.put("AQUA", 0xff00ffff);
		constants.put("TEAL", 0xff008080);
		constants.put("BLUE", 0xff0000ff);
		constants.put("NAVY", 0xff000080);
		constants.put("FUCHSIA", 0xffff00ff);
		constants.put("PURPLE", 0xff800080);
		
		keywords = new HashSet<>();
		keywords.add("X");
		keywords.add("Y");
		keywords.add("width");
		keywords.add("height");
		keywords.add("screen");
		keywords.add("screen_width");
		keywords.add("screen_height");
		keywords.add("image");
		keywords.add("int");
		keywords.add("string");
		keywords.add("red");
		keywords.add("green");
		keywords.add("blue");
	}
	
	/**
	 * Returns a String representation of the list of Tokens.
	 * You may modify this as desired. 
	 */
	public String toString() {
		return tokens.toString();
	}
}
