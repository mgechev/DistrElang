package org.mgechev.distrelang.messages;

import java.util.ArrayList;

import org.mgechev.elang.parser.expressions.IExpression;

public class Invoke extends Message {
    public ArrayList<IExpression> args;
    public String name;
    
    public Invoke() {
        this.type = MessageTypes.INVOKE;
    }
}
