package org.mgechev.elang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.mgechev.distrelang.Scheduler;
import org.mgechev.elang.interpreter.Interpreter;
import org.mgechev.elang.lexer.Lexer;
import org.mgechev.elang.parser.Parser;
import org.mgechev.elang.tokens.KeyWordToken;
import org.mgechev.elang.tokens.Token;

public class ELang {
    
    private static String program = "";
    
    public static void loadProgramFile(String file) {
        try {
            File programFile = new File(file);
            FileReader fileReader = new FileReader(programFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String programLine;
            while ((programLine = reader.readLine()) != null) {
                program += programLine;
            }
            reader.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void loadProgramString(String currentProgram) {
        program = currentProgram;
    }
    
    public static void execute(Scheduler scheduler) {
        Lexer l = new Lexer(program);
        ArrayList<Token> lst = l.lex();
        ArrayList<Token> fn;
        for (Token token : lst) {
            if (token instanceof KeyWordToken && token.value().equals("def")) {
                fn = new ArrayList<Token>();
                fn.add(token);
            }
            if (token instanceof KeyWordToken && token.value().equals("enddef")) {
                //
            }
        }
        
        Parser parser = new Parser(lst);
        parser.parse();
        
        Interpreter interpreter = new Interpreter(parser.getStatements());
        interpreter.interpret();
    }

}
