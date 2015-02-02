package org.mgechev.elang.parser.expressions.symbols.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.mgechev.elang.common.Program;
import org.mgechev.elang.interpreter.Interpreter;
import org.mgechev.elang.parser.expressions.IExpression;
import org.mgechev.elang.parser.expressions.symbols.Function;
import org.mgechev.elang.parser.expressions.symbols.Value;
import org.mgechev.elang.parser.expressions.symbols.Variable;
import org.mgechev.elang.parser.statements.IStatement;
import org.mgechev.elang.parser.statements.ReturnStatement;
import org.mgechev.elang.parser.statements.ReturnStatementException;

public class CustomFunction extends Function {
    protected ArrayList<Variable> funcArgs;
    protected ArrayList<IStatement> statements;
    protected String name;
    protected Program program;
    
    public CustomFunction(Program program) {
        this(program, 0);
    }
    
    public CustomFunction(Program program, int args) {
        super(args);
        this.funcArgs = null;
        this.statements = null;
        this.program = program;
    }
    
    //funcVars are the variables which must be declared for the function
    //args are the arguments which are put for evaluation
    public CustomFunction(ArrayList<Variable> funcArgs, ArrayList<IStatement> statements) {
        super(funcArgs.size());
        this.funcArgs = funcArgs;
        this.statements = statements;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Value evaluate() {
        HashMap<String, Value> vars = new HashMap<String, Value>();
        Value result = null;
        Value value;
        String name;
        int current = 0;
        while (!this.args.isEmpty()) {
            value = ((IExpression)this.args.pop()).evaluate();
            name = this.funcArgs.get(current).getValue(); 
            vars.put(name, value);
            current += 1;
        }
        
        program.pushScope(vars);
        
        try {
            Interpreter interpreter = new Interpreter(this.statements);
            interpreter.interpret();
        } catch (ReturnStatementException e) {
            result = e.getResult();
        }
        
        program.popScope();
        
        return result;
    }
    
    public void setArguments(ArrayList<Variable> args) {
        this.funcArgs = args;
        this.argsCount = args.size();
    }
    
    public void setStatements(ArrayList<IStatement> statements) {
        this.statements = statements;
    }

}
