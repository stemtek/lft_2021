import src.*;
import src_translate.*;
import java.io.*;
import java.util.*;

// Es Traduttore 5.1
public class Translator {
    private Lexer lex; // utilizzo il Lexer 2.3
    private BufferedReader pbr;
    private Token look;
    
    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;

    public Translator(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
		throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
	if (look.tag == t) {
	    if (look.tag != Tag.EOF) move();
	} else error("syntax error");
    }

    public void prog() {        
		if(look.tag == '=' || look.tag == 266 || look.tag == 267 || look.tag == 259 || look.tag == 263 || look.tag == 123) {
        int lnext_prog = code.newLabel();
        statlist(lnext_prog);
        code.emitLabel(lnext_prog);
        match(Tag.EOF);
        try {
        	code.toJasmin();
        }
        catch(java.io.IOException e) {
        	System.out.println("IO error\n");
        };
		
		} else error("Error in prog");
    }

	private void statlist(int lnext_prog) {
		if(look.tag == '=' || look.tag == 266 || look.tag == 267 || look.tag == 259 || look.tag == 263 || look.tag == 123) {
			stat(lnext_prog);
			statlistp();
		}
		else error("Error in statlist\n");
	}
		
	private void statlistp() {
		if(look.tag == ';' || look.tag == '=' || look.tag == 266 || look.tag == 267 || look.tag == 259 || look.tag == 263 || look.tag == 123){
		   int lnext_prog = code.newLabel();
           match(';');
           stat(lnext_prog);
           statlistp();
        }
		 else if(look.tag == Tag.EOF || look.tag == '}'){
        }
		 else error("Error in statlistp\n");
    }

    public void stat(int lnext_prog) {
        switch(look.tag) {
			case '=':
                if (look.tag == '=') {
                    int id_addr = st.lookupAddress(((Word)look).lexeme);
                    if (id_addr == -1) {
                        id_addr = count;
                        st.insert(((Word)look).lexeme, count++);
                    }
					match(Token.assign.tag);
                    match(Tag.ID); 
                    expr();
                    code.emit(OpCode.istore, id_addr);
                } else {
                    error("Error in grammar (stat) after '='( with " + look);
                }
					break;
			
			case Tag.PRINT:
				match(Tag.PRINT);
				match('(');
                exprlist();
                code.emit(OpCode.invokestatic, 1);
                match(')');
                break;
	
            case Tag.READ:
                match(Tag.READ);
                match('(');
                if (look.tag == Tag.ID) {
                    int id_addr = st.lookupAddress(((Word)look).lexeme);
                    if (id_addr == -1) {
                        id_addr = count;
                        st.insert(((Word)look).lexeme,count++);
                    }                    
                    match(Tag.ID);
                    match(')');
                    code.emit(OpCode.invokestatic,0);
                    code.emit(OpCode.istore,id_addr);   
                }
                else
                    error("Error in grammar (stat) after read( with " + look);
                break;
			
			case Tag.COND:
				match(Tag.COND);
				whenlist(lnext_prog);
				match(Tag.ELSE);
                stat(lnext_prog);
			    code.emitLabel(lnext_prog);
			    break;
				
			
			case Tag.WHILE:
				match(Tag.WHILE);
                match('(');
				int btrue = code.newLabel();
				int bfalse = code.newLabel();
				bexpr(btrue,bfalse);
				match(')');
				code.emitLabel(btrue);
                stat(lnext_prog);
				code.emit(OpCode.GOto,lnext_prog);
				code.emitLabel(bfalse);
				break;
			
			case '{':
                match('(');
                statlist(lnext_prog);
                match(')');
                break;
				
			default:
				error("Error in stat\n");
				break;
			
        }
     }

	private void whenlist(int lnext_prog) {
		if(look.tag == 260) {
			whenitem(lnext_prog);
			whenlistp(lnext_prog);
		} else error("Error in whenlist\n");
	}
	
	private void whenlistp(int lnext_prog) {
		if(look.tag == 260) {
			whenitem(lnext_prog);
			whenlistp(lnext_prog);
		} else if ( look.tag == 262){} 
		  else error("Error in whenlistp\n");
	}
	
	private void whenitem(int lnext_prog) {
		if(look.tag == 260) {
			match(260);
			match('(');
			int btrue = code.newLabel();
			int bfalse = code.newLabel();
			bexpr(btrue,bfalse);
			match(')');
			match(264);
			code.emitLabel(btrue);
			stat(lnext_prog);
			code.emit(OpCode.GOto, lnext_prog);
	        code.emitLabel(bfalse);
		} 
			else error("Error in whenitem\n");
	
	}

    private void bexpr(int btrue, int bfalse) {
        switch (look.tag) {
            case '(':
            case 256:
            case 257:
                expr();
                if (look == Word.lt) {
                    match(Tag.RELOP);
                    expr();
                    code.emit(OpCode.if_icmplt, btrue);
                    code.emit(OpCode.GOto, bfalse);
                } else if (look == Word.gt) {
                    match(Tag.RELOP);
                    expr();
                    code.emit(OpCode.if_icmpgt, btrue);
                    code.emit(OpCode.GOto, bfalse);
                } else if (look == Word.eq) {
                    match(Tag.RELOP);
                    expr();
                    code.emit(OpCode.if_icmpeq, btrue);
                    code.emit(OpCode.GOto, bfalse);
                } else if (look == Word.le) {
                    match(Tag.RELOP);
                    expr();
                    code.emit(OpCode.if_icmple, btrue);
                    code.emit(OpCode.GOto, bfalse);
                }  else if (look == Word.ne) {
                    match(Tag.RELOP);
                    expr();
                    code.emit(OpCode.if_icmpne, btrue);
                    code.emit(OpCode.GOto, bfalse);
                } else if (look == Word.ge) {
                    match(Tag.RELOP);
                    expr();
                    code.emit(OpCode.if_icmpge, btrue);
                    code.emit(OpCode.GOto, bfalse);
                } else {
                    error("Error\n");
                }
                break;

            default:
                error("Error in bexpr\n");
        }
    }

    private void expr() {
        switch(look.tag) {
			
			case '+':
				match('+');
				match('(');
				exprlist();
				match(')');
				code.emit(OpCode.iadd);
				break;
			
            case '-':
                match('-');
                expr();
                expr();
                code.emit(OpCode.isub);
                break;
			
			case '*':
				match('*');
				match('(');
				exprlist();
				match(')');
				code.emit(OpCode.imul);
				break;
				
			case '/':
                match('/');
                expr();
                expr();
                code.emit(OpCode.idiv);
                break;
				
			case 256:
				code.emit(OpCode.ldc, Integer.parseInt(((src.NumberTok)look).number));
                match(256);
				break;
				
			case 257:
				int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr == -1) {
                    error("Error ID nel metodo expr\n");
                }
                move();
                code.emit(OpCode.iload, id_addr);
                break;
				
			 default:
                error("Error in expr\n");
	
        }
    }
	
    private void exprlist() {
		if(look.tag == 43 || look.tag == 45 || look.tag == 42 || look.tag == 47 || look.tag == 256 || look.tag == 257){
			expr();
			exprlistp();
		}
		else error("Error in exprlist\n");
    }
	
	private void exprlistp() {
		if(look.tag == 43 || look.tag == 45 || look.tag == 42 || look.tag == 47 || look.tag == 256 || look.tag == 257){
			expr();
			exprlistp();
		} else if ( look.tag == ')'){}
		  else error("Error in exprlistp\n");	
	}
	
	public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "test.lft"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}

