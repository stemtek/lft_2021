import java.io.*; 
import java.util.*;
import src.*;

// Parser 3.2
public class Parser2 {
    private Lexer lex; // utilizzo il Lexer 2.3
    private BufferedReader pbr;
    private Token look;

    public Parser2(Lexer l, BufferedReader br) {
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
			statlist();
			match(Tag.EOF);
		}
		else error("Error in prog\n");
	}
	
	private void statlist() {
		if(look.tag == '=' || look.tag == 266 || look.tag == 267 || look.tag == 259 || look.tag == 263 || look.tag == 123) {
			stat();
			statlistp();
		}
		else error("Error in statlist\n");
	}
		
	private void statlistp() {
		if(look.tag == ';' || look.tag == '=' || look.tag == 266 || look.tag == 267 || look.tag == 259 || look.tag == 263 || look.tag == 123){
           match(';');
           stat();
           statlistp();
        }
		 else if(look.tag == Tag.EOF || look.tag == '}'){
        }
		 else error("Error in statlistp\n");
    }
	
	private void stat() {
		switch(look.tag) {
			case '=':
				match('=');
                match(257);
                expr();
				break;
			
			case 266:
				match(266);
                match('(');
                exprlist();
                match(')');
				break;
				
			case 267:
				match(267);
				match('(');
                match(257);
                match(')');
               	break;
				
			case 259:
				match(259);
				whenlist();
				match(262);
				stat();
				break;
				
			case 263:
				match(263);
                match('(');
                bexpr();
                match(')');
                stat();
               	break;
				
			case '{':
				match('{');
                statlist();
                match('}');
                break;
			
			default:
				error("Error in stat\n");
				break;
		}
	}

	private void whenlist() {
		if(look.tag == 260) {
			whenitem();
			whenlistp();
		} else error("Error in whenlist\n");
	}
	
	private void whenlistp() {
		if(look.tag == 260) {
			whenitem();
			whenlistp();
		} else if ( look.tag == 262){} 
		  else error("Error in whenlistp\n");
	}
	
	private void whenitem() {
		if(look.tag == 260) {
			match(260);
			match('(');
			bexpr();
			match(')');
			match(264);
			stat();
		} 
			else error("Error in whenitem\n");
	
	}

	private void bexpr() {
		if (look.tag == 258){
			match(258);
            expr();
            expr();
        }else error("Error in bexpr\n");
	}
	
	private void expr() {
			switch (look.tag) {
			case 43:
				match(43);
				match('(');
				exprlist();
				match(')');
				break;
				
			case 45:
				match(45);
				expr();
				expr();
				break;
				
			case 42:
				match(42);
				match('(');
				exprlist();
				match(')');
				break;
				
			case 47:
				match(47);
				expr();
				expr();
				break;
				
			case 256:
				match(256);
				break;
				
			case 257:
				match(257);
				break;
				
			default:
				error ("Error in expr");
				break;
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
        String path = "parser2.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser2 parser = new Parser2(lex, br);
            parser.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}
