package org.mgechev.elang.parser.expressions.symbols;

import org.mgechev.elang.common.Program;
import org.mgechev.elang.parser.expressions.IExpression;

public class Variable extends Symbol<String> implements IExpression {
    
    private Program program;
    
    public Variable(String name, Value value, Program program) {
        this(name, program);
        this.program = program;
        program.setVal(this.value, value);
    }
    
    public Variable(String name, Program program) {
        super(name);
        this.program = program;
    }
    
    public Value evaluate() {
        return program.getVar(this.value);
    }
    
    public void setValue(Value val) {
        program.setVal(this.value, val);
    }
    
}
