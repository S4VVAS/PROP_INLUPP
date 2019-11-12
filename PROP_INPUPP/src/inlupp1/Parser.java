package inlupp1;

import java.io.IOException;

public class Parser implements IParser {
	Tokenizer token;

	@Override
	public void open(String fileName) throws IOException, TokenizerException {
		token = new Tokenizer();
		token.open(fileName);
		
	}

	@Override
	public INode parse() throws IOException, TokenizerException, ParserException {
		return new AssignmentNode();
	}

	@Override
	public void close() throws IOException {
		if(token != null)
			token.close();
	}
	
	private String tabs(int amt) {
		String tabs = "";
		for(int i = 0; i < amt; i++) {
			tabs = tabs + "\t";
		}
		return tabs;
	}
	
	private void build(StringBuilder builder, int tabs) {
		builder.append(tabs(tabs) + token.current().token() + " " + token.current().value() + "\n");
	}
	
	//______________________________________________________________________________________________
	//______________________________________________________________________________________________
	//______________________________________________________________________________________________
	
	class AssignmentNode implements INode {
		String id;
		char assign;
		char semCol;
		INode exp;
		
		@Override
		public Object evaluate(Object[] args) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs) throws IOException, TokenizerException, ParserException {
			builder.append("AssignmentNode\n");
			token.moveNext();
			if(token.current().token() == Token.IDENT) {
				build(builder, tabs);
				token.moveNext();
				if(token.current().token() == Token.ASSIGN_OP) {
					build(builder, tabs);
					exp = new ExpressionNode();
					exp.buildString(builder, tabs + 1);
					
					token.moveNext();
					if(token.current().token() == Token.SEMICOLON)
						build(builder, tabs);
					else
						throw new ParserException("Semicolon missing from assignment: " + token.current().value());
				
				}
				else
					throw new ParserException("Assignment of id missing: " + token.current().value());
			} else
					throw new ParserException("Wrong id: " + token.current().value());	
		}

	}
	//______________________________________________________________________________________________
	
	class ExpressionNode implements INode {
		INode term, exp;
		char arOp;

		@Override
		public Object evaluate(Object[] args) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs) throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "ExpressionNode\n");
			
			term = new TermNode();
			term.buildString(builder, tabs + 1);
			
			token.moveNext();
			if(token.current().token() == Token.ADD_OP || token.current().token() == Token.SUB_OP)
				build(builder, tabs);
			else
				throw new ParserException("Arithmetic Operator missing(+/-): " + token.current().value());
			
			exp = new ExpressionNode();
			exp.buildString(builder, tabs + 1);
		}
	}
	//______________________________________________________________________________________________
	
	class TermNode implements INode {
		INode fact, term;
		char arOp;

		@Override
		public Object evaluate(Object[] args) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs) throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "TermNode\n");
			
			fact = new FactorNode();
			fact.buildString(builder, tabs + 1);
			
			token.moveNext();
			if(token.current().token() == Token.MULT_OP || token.current().token() == Token.DIV_OP)
				build(builder, tabs);
			else
				throw new ParserException("Arithmetic Operator missing(*//): " + token.current().value());
			
			term = new TermNode();
			term.buildString(builder, tabs + 1);
		}

	}
	//______________________________________________________________________________________________
	
	class FactorNode implements INode {
		int num;
		char lP, rP;
		INode exp;

		@Override
		public Object evaluate(Object[] args) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void buildString(StringBuilder builder, int tabs) throws IOException, TokenizerException, ParserException {
			builder.append(tabs(tabs - 1) + "FactorNode\n");
			
			token.moveNext();
			if(token.current().token() == Token.INT_LIT || token.current().token() == Token.IDENT) {
				build(builder, tabs);
			}
			else if(token.current().token() == Token.LEFT_PAREN) {
				build(builder, tabs);
				
				exp = new ExpressionNode();
				exp.buildString(builder, tabs + 1);
				
				token.moveNext();
				if(token.current().token() == Token.LEFT_PAREN) {
					build(builder, tabs);
				}
				else
					throw new ParserException("Closing parentheses missing: " + token.current().value());

			}
			else
				throw new ParserException("False factor: " + token.current().value());
			
			
		}

	}

}





































