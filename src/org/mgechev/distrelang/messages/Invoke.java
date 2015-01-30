package org.mgechev.distrelang.messages;

import java.util.ArrayList;

import org.mgechev.elang.parser.expressions.symbols.Value;

public class Invoke extends Message {
    public ArrayList<Value> args;
    public String name;
    
    public Invoke() {
        this.type = MessageTypes.INVOKE;
    }
}
