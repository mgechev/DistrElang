package org.mgechev.elang.tokens;

public abstract class Token<T> {

    protected T value;
    
    public Token(T val) {
        this.value = val;
    }
    
    public T value() {
        return value;
    }
    
}
