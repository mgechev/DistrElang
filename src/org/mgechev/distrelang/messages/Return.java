package org.mgechev.distrelang.messages;

import org.mgechev.elang.parser.expressions.symbols.Value;

public class Return extends Message {
    public String name;
    public Value result;
    
    public Return() {
        this.type = MessageTypes.RETURN;
    }
}
