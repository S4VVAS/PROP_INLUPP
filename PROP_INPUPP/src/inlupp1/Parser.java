package inlupp1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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

	// ______________________________________________________________________________________________
	// ______________________________________________________________________________________________
	// ______________________________________________________________________________________________

	class BlockNode implements INode {

		INode stm;
		Lexeme rB, lB;
		Object[] variables;
		

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

		@Override
		public Object evaluate(Object[] args) throws Exception {
			variables = new Object[((StatementNode) stm).getInstances()];
			variables = (Object[]) stm.evaluate(variables);
			
			String evaluation = "";
			
			for(int i = 0; i < variables.length; i++) {
				System.out.println(((AssignedValue) variables[i]).getValue());
				evaluation = evaluation + ((AssignedValue) variables[i]).getId() + " = " +((AssignedValue) variables[i]).getValue() + "\n";
			}
			
			return evaluation;
		}
	}

	// ______________________________________________________________________________________________
	class StatementNode implements INode {
		INode assign, stm;
		

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
		
		public int getInstances() {
			if(stm != null)
				return ((StatementNode) stm).getInstances() + 1;
			return 0;
		}

		@Override
		public Object evaluate(Object[] args) throws Exception {
			try {
				return stm.evaluate((Object[]) assign.evaluate(args));
			}catch(Exception e) {
				
			}
			return assign.evaluate(args);
		}
	}

	// ______________________________________________________________________________________________
	class AssignmentNode implements INode {

		Lexeme assign, semiCol, id;
		INode exp;

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

		@Override
		public Object evaluate(Object[] args) throws Exception {
			for(int i = 0; i < args.length; i++) {
				if(args[i] == null) {
					args[i] = new AssignedValue(id, (Lexeme) exp.evaluate(args));
					return args;
				}
			}
			return args;
		}

	}
	// ______________________________________________________________________________________________

	class ExpressionNode implements INode {
		INode term, exp;
		Lexeme arOp;

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

		@Override
		public Object evaluate(Object[] args) throws Exception {
			
			if (term != null && exp == null && arOp == null)
				return term.evaluate(args);
			else if (term != null && exp != null && arOp != null) {
				if (arOp.token() == Token.ADD_OP) {
					return new Lexeme((double) ((Lexeme) term.evaluate(args)).value() + (double) ((Lexeme) exp.evaluate(args)).value(),
							Token.INT_LIT);
				} else if (arOp.token() == Token.SUB_OP) {
					return new Lexeme((double) ((Lexeme) term.evaluate(args)).value() - (double) ((Lexeme) exp.evaluate(args)).value(),
							Token.INT_LIT);
				}
			}
			return null;

		}
	}
	// ______________________________________________________________________________________________

	class TermNode implements INode {
		INode fact, term;
		Lexeme arOp;

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

		@Override
		public Object evaluate(Object[] args) throws Exception {

			if (fact != null && term == null && arOp == null)
				return fact.evaluate(args);
			else if (fact != null && term != null && arOp != null) {
				if (arOp != null && arOp.token() == Token.MULT_OP) {
					return new Lexeme((double) ((Lexeme) fact.evaluate(args)).value() * (double) ((Lexeme) term.evaluate(args)).value(),
							Token.INT_LIT);
				} else if (arOp.token() == Token.DIV_OP) {
					return new Lexeme((double) ((Lexeme) fact.evaluate(args)).value() / (double) ((Lexeme) term.evaluate(args)).value(),
							Token.INT_LIT);
				}
			}
			return null;
		}

	}
	// ______________________________________________________________________________________________

	class FactorNode implements INode {
		Lexeme lex;
		INode exp;

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

		@Override
		public Object evaluate(Object[] args) throws Exception {
			if (lex != null && lex.token() == Token.INT_LIT)
				return lex;
			else if(lex != null && lex.token() == Token.IDENT) {
				for(Object o: args) {
					if(((AssignedValue) o).getId().equals(lex.value())) {
						return new Lexeme(((AssignedValue) o).getValue(), Token.INT_LIT);
					}
				}
			}
			else if (exp != null) {
				return exp.evaluate(args);
			}
			return null;
		}

	}

}
