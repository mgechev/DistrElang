package org.mgechev.distrelang;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mgechev.distrelang.messages.RegisterComplete;
import org.mgechev.distrelang.messages.RegisterFunction;
import org.mgechev.distrelang.messages.RemoteFunctionData;
import org.mgechev.distrelang.messages.SymbolTable;
import org.mgechev.elang.tokens.Token;

public class Scheduler {
    
    private ArrayList<InetSocketAddress> hosts;
    private Map<RemoteFunctionData, InetSocketAddress> symbolTable;
    private int current = 0;
    
    public Scheduler(ArrayList<InetSocketAddress> hosts) {
        this.hosts = hosts;
        symbolTable = new HashMap<RemoteFunctionData, InetSocketAddress>();
    }
    
    public void register(ArrayList<Token> tokens) throws IOException {
        InetSocketAddress host = this.hosts.get(current % this.hosts.size());
        current += 1;
        RegisterFunction msg = new RegisterFunction();
        msg.tokens = tokens;
        ConnectionProxy.Get().send(msg, host);
        RegisterComplete res = (RegisterComplete)ConnectionProxy.Get().read(host, RegisterComplete.class);
        System.out.println("Function registered name: " + res.data.name);
        RemoteFunctionData f = res.data;
        this.symbolTable.put(f, host);
    }
    
    public Map<RemoteFunctionData, InetSocketAddress> done() throws IOException {
        SymbolTable table = new SymbolTable();
        table.args = new HashMap<String, Integer>();
        table.table = new HashMap<String, InetSocketAddress>();
        
        for (RemoteFunctionData data : this.symbolTable.keySet()) {
            table.args.put(data.name, data.argsCount);
            table.table.put(data.name, this.symbolTable.get(data));
        }
        for (InetSocketAddress addr : this.hosts) {
            ConnectionProxy.Get().send(table, addr);;
        }
        return this.symbolTable;
    }

}
