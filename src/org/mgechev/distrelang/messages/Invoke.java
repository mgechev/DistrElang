package org.mgechev.distrelang.messages;

import java.util.ArrayList;

import org.mgechev.elang.parser.expressions.symbols.Variable;

public class Invoke extends Message {
    public ArrayList<Variable> args;
    public String name;
    
    public Invoke() {
        this.type = MessageTypes.INVOKE;
    }
}
