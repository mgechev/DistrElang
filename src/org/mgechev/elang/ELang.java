package org.mgechev.elang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

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
    
    public static void execute(Scheduler scheduler) throws IOException {
        Lexer l = new Lexer(program);
        ArrayList<Token> lst = l.lex();
        ArrayList<Token> fn = null;
        boolean functionDef = false;
        Iterator<Token> iter = lst.iterator();
        while (iter.hasNext()) {
            Token token = iter.next();
            if (token instanceof KeyWordToken && token.value().equals("def")) {
                fn = new ArrayList<Token>();
                functionDef = true;
            }
            if (token instanceof KeyWordToken && token.value().equals("enddef")) {
                fn.add(token);
                fn.add(iter.next());
                scheduler.register(fn);
                functionDef = false;
            }
            if (functionDef) {
                fn.add(token);
            }
        }        
        Parser parser = new Parser(lst);
        parser.parse();
        
        Interpreter interpreter = new Interpreter(parser.getStatements());
        interpreter.interpret();
    }

}
