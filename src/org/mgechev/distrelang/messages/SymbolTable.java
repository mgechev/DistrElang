package org.mgechev.distrelang.messages;

import java.net.InetSocketAddress;
import java.util.Map;

public class SymbolTable extends Message {
    public Map<String, InetSocketAddress> table;
    public Map<String, Integer> args;
    
    public SymbolTable() {
        this.type = MessageTypes.SYMBOL_TABLE;
    }
}
