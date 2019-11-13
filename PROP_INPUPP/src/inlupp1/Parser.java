package inlupp1;

import java.io.IOException;
import java.util.ArrayList;

public class Parser implements IParser {
	Tokenizer token;

	@Override
	public void open(String fileName) throws IOException, TokenizerException {
		token = new Tokenizer();
		token.open(fileName);

	}

	@Override
	public INode parse() throws IOException, TokenizerException, ParserException {
		return new BlockNode();
	}

	@Override
	public void close() throws IOException {
		if (token != null)
			token.close();
	}

	private String tabs(int amt) {
		String tabs = "";
		for (int i = 0; i < amt; i++) {
			tabs = tabs + "\t";
		}
		return tabs;
	}

	private void build(StringBuilder builder, int tabs) {
		builder.append(tabs(tabs) + token.current().token() + " " + token.current().value() + "\n");
	}
	
	class AssignedValue{
		Lexeme id;
		Lexeme value;
		
		public AssignedValue(Lexeme id, Lexeme value) {
			this.id = id;
			this.value = value;
		}
	}

	// ______________________________________________________________________________________________
	// ______________________________________________________________________________________________
	// ______________________________________________________________________________________________
	class BlockNode implements INode {

		INode stm;
		Lexeme rB, lB;

		@Override
		public Lexeme evaluate() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs)
				throws IOException, TokenizerException, ParserException {
			builder.append("BlockNode\n");

			token.moveNext();
			if (token.current().token() == Token.LEFT_CURLY) {
				token.moveNext();

				stm = new StatementNode();
				stm.buildString(builder, tabs + 1);

				if (token.current().token() == Token.RIGHT_CURLY) {
					build(builder, tabs);
					token.moveNext();
				} else
					throw new ParserException(
							"Block must be contained within brackets {-BLOCK-}. Closing bracket missing");
			} else
				throw new ParserException("Block must be contained within brackets {-BLOCK-}. Opening bracket missing");

		}
	}

	// ______________________________________________________________________________________________
	class StatementNode implements INode {
		INode assign, stm;
		ArrayList<AssignedValue> ass = new ArrayList<AssignedValue>();

		@Override
		public Lexeme evaluate() throws Exception {
			return null;
		}
		
		public ArrayList<AssignedValue> getId() {
			return ass;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs)
				throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "StatementNode\n");

			try {

				assign = new AssignmentNode();
				assign.buildString(builder, tabs + 1);
				

				stm = new StatementNode();
				stm.buildString(builder, tabs + 1);

			} catch (Exception e) {
			}

		}
	}

	// ______________________________________________________________________________________________
	class AssignmentNode implements INode {

		Lexeme assign, semiCol, id;
		INode exp;

		@Override
		public Lexeme evaluate() throws Exception {
			return null;
		}
		
		public AssignedValue getId() throws Exception {
			return new AssignedValue(id, exp.evaluate());
		}

		@Override
		public void buildString(StringBuilder builder, int tabs)
				throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "AssignmentNode\n");

			// token.moveNext();
			// //----------------------------------------------------------------------------------------------------------------------------------------------
			if (token.current().token() == Token.IDENT) {
				id = token.current();
				build(builder, tabs);
				token.moveNext();
				if (token.current().token() == Token.ASSIGN_OP) {
					assign = token.current(); // UNNESSESARY ATM
					build(builder, tabs);
					token.moveNext();

					exp = new ExpressionNode();
					exp.buildString(builder, tabs + 1);

					if (token.current().token() == Token.SEMICOLON) {
						semiCol = token.current(); // UNNESSESARY ATM
						build(builder, tabs);
						token.moveNext();
					} else
						throw new ParserException("Semicolon missing from assignment: " + token.current().value());

				} else
					throw new ParserException("Assignment of id missing: " + token.current().value());
			} else
				throw new ParserException("Wrong id: " + token.current().value());
		}

	}
	// ______________________________________________________________________________________________

	class ExpressionNode implements INode {
		INode term, exp;
		Lexeme arOp;

		@Override
		public Lexeme evaluate() throws Exception {

			if (term != null && exp == null && arOp == null)
				return term.evaluate();
			else if (term != null && exp != null && arOp != null) {
				if (arOp.token() == Token.ADD_OP) {
					return new Lexeme((double) term.evaluate().value() + (double) exp.evaluate().value(),
							Token.INT_LIT);
				} else if (arOp.token() == Token.SUB_OP) {
					return new Lexeme((double) term.evaluate().value() - (double) exp.evaluate().value(),
							Token.INT_LIT);
				}
			}
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs)
				throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "ExpressionNode\n");

			term = new TermNode();
			term.buildString(builder, tabs + 1);

			if (token.current().token() == Token.ADD_OP || token.current().token() == Token.SUB_OP) {
				arOp = token.current();
				build(builder, tabs);
				token.moveNext();

				exp = new ExpressionNode();
				exp.buildString(builder, tabs + 1);
			}
		}
	}
	// ______________________________________________________________________________________________

	class TermNode implements INode {
		INode fact, term;
		Lexeme arOp;

		@Override
		public Lexeme evaluate() throws Exception {

			if (fact != null && term == null && arOp == null)
				return fact.evaluate();
			else if (fact != null && term != null && arOp != null) {
				if (arOp.token() == Token.MULT_OP) {
					return new Lexeme((double) fact.evaluate().value() * (double) term.evaluate().value(),
							Token.INT_LIT);
				} else if (arOp.token() == Token.DIV_OP) {
					return new Lexeme((double) fact.evaluate().value() / (double) term.evaluate().value(),
							Token.INT_LIT);
				}
			}
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs)
				throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "TermNode\n");

			fact = new FactorNode();
			fact.buildString(builder, tabs + 1);

			if (token.current().token() == Token.MULT_OP || token.current().token() == Token.DIV_OP) {
				arOp = token.current();
				build(builder, tabs);
				token.moveNext();

				term = new TermNode();
				term.buildString(builder, tabs + 1);
			}
		}

	}
	// ______________________________________________________________________________________________

	class FactorNode implements INode {
		Lexeme lex;
		INode exp;

		@Override
		public Lexeme evaluate() throws Exception {

			if (lex != null)
				return lex;
			else if (exp != null) {
				return exp.evaluate();
			}
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs)
				throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "FactorNode\n");

			if (token.current().token() == Token.INT_LIT || token.current().token() == Token.IDENT) {
				lex = token.current();
				build(builder, tabs);
				token.moveNext();
			} else if (token.current().token() == Token.LEFT_PAREN) {
				build(builder, tabs);
				token.moveNext();

				exp = new ExpressionNode();
				exp.buildString(builder, tabs + 1);

				if (token.current().token() == Token.RIGHT_PAREN) {
					build(builder, tabs);
					token.moveNext();
				} else
					throw new ParserException("Closing parentheses missing: " + token.current().value());

			} else
				throw new ParserException("False factor: " + token.current().value());

		}

	}

}
