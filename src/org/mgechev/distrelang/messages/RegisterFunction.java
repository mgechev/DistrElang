package org.mgechev.distrelang.messages;

import java.util.ArrayList;

import org.mgechev.elang.tokens.Token;

public class RegisterFunction extends Message {
    public ArrayList<Token> tokens;
    
    
    public RegisterFunction() {
        this.type = MessageTypes.REGISTER;
    }
}
