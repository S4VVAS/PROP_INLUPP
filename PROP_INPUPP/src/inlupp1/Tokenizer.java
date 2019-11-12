package inlupp1;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Tokenizer implements ITokenizer {

	private static Map<Character, Token> symbols = new HashMap<Character, Token>();
	
	private Scanner scanner = null;
	private Lexeme current = null;
	private Lexeme next = null;
	
	public Tokenizer() {
	
		symbols.put(new Character('+'), Token.ADD_OP);
		symbols.put(new Character('-'), Token.SUB_OP);
		symbols.put(new Character('*'), Token.MULT_OP);
		symbols.put(new Character('/'), Token.DIV_OP);
		symbols.put(new Character('='), Token.ASSIGN_OP);
		symbols.put(new Character('('), Token.LEFT_PAREN);
		symbols.put(new Character(')'), Token.RIGHT_PAREN);
		symbols.put(new Character(';'), Token.SEMICOLON);
		symbols.put(new Character('{'), Token.LEFT_CURLY);
		symbols.put(new Character('}'), Token.RIGHT_CURLY);
	}
	
	@Override
	public void open(String fileName) throws IOException, TokenizerException {
		scanner = new Scanner();
		scanner.open(fileName);
		scanner.moveNext();
		next = extractLexeme();
		
	}

	@Override
	public Lexeme current() {
		return current;
	}

	@Override
	public void moveNext() throws IOException, TokenizerException {
		if (scanner == null)
			throw new IOException("No open file.");
		current = next;
		if (next.token() != Token.EOF)
			next = extractLexeme();
	}
	
	private void consumeWhiteSpaces() {
		while (Character.isWhitespace(scanner.current())){
		    try {
				scanner.moveNext();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Lexeme extractLexeme() throws TokenizerException, IOException {
		consumeWhiteSpaces();
		Character ch = scanner.current();
		
		
		if(ch == Scanner.EOF || ch == null) {
			scanner.moveNext();
			return new Lexeme(ch, Token.EOF);
		}
		
		else if(Character.isAlphabetic(ch)) {
			String s = "";
			do {
				s = s + scanner.current();
				scanner.moveNext();
			}while(Character.isAlphabetic(scanner.current()));
			return new Lexeme(s, Token.IDENT);
		}
		
		else if(Character.isDigit(ch)) {
			String s = "";
			do {
				s = s + scanner.current();
				scanner.moveNext();
			}while(Character.isDigit(scanner.current()));
			return new Lexeme(Integer.parseInt(s), Token.INT_LIT);
		}
		
		else if(symbols.containsKey(ch)) {
			scanner.moveNext();
			return new Lexeme(ch, symbols.get(ch));
		}
		
		throw new TokenizerException("Unknown character: " + String.valueOf(ch));
		
	}

	@Override
	public void close() throws IOException {
		scanner.close();
	}

}