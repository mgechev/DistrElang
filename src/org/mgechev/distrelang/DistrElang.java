package org.mgechev.distrelang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mgechev.distrelang.messages.RemoteFunctionData;
import org.mgechev.elang.common.Program;
import org.mgechev.elang.interpreter.Interpreter;
import org.mgechev.elang.lexer.Lexer;
import org.mgechev.elang.parser.Parser;
import org.mgechev.elang.parser.expressions.symbols.Function;
import org.mgechev.elang.tokens.KeyWordToken;
import org.mgechev.elang.tokens.Token;

public class DistrElang {
    
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
        
        Map<RemoteFunctionData, InetSocketAddress> symbolTable = scheduler.done();
        Map<String, Function> lookup = new HashMap<String, Function>();
        for (RemoteFunctionData fun : symbolTable.keySet()) {
            RemoteFunction f = new RemoteFunction(symbolTable.get(fun), fun.argsCount, scheduler.getProxy());
            f.setName(fun.name);
            lookup.put(fun.name, f);
        }
        
        
        Parser parser = new Parser(lst, lookup);
        parser.parse();
        
        Function f = Program.Get().getFunction("sum");
        
        Interpreter interpreter = new Interpreter(parser.getStatements());
        interpreter.interpret();
        
        for (Socket s : scheduler.getProxy().getSockets()) {
            if (s != null && !s.isClosed()) {
                s.close();
            }
        }
    }

}
