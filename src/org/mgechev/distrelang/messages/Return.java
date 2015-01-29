package org.mgechev.distrelang.messages;

import org.mgechev.elang.parser.expressions.IExpression;

public class Return extends Message {
    public String name;
    public IExpression result;
    
    public Return() {
        this.type = MessageTypes.RETURN;
    }
}
