package org.mgechev.elang.tokens;

public class NumberToken extends Token<Double> {
    
    public NumberToken(String symbol) {
        super(Double.parseDouble(symbol));
    }

}
