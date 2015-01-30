package org.mgechev.elang.parser.expressions.symbols;

import java.util.Stack;


public abstract class Function extends Evaluator {
    
    protected int argsCount;
    
    public Function(int argsCount) {
        this.argsCount = argsCount;
    }
    
    public int getArgumentsCount() {
        return this.argsCount;
    }
    
    public void reset() {
        this.args = new Stack<Variable>();
    }
    
}
