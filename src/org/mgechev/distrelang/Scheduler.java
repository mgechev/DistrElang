package org.mgechev.distrelang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

import org.mgechev.distrelang.messages.Invoke;
import org.mgechev.distrelang.messages.Message;
import org.mgechev.distrelang.messages.RegisterComplete;
import org.mgechev.distrelang.messages.RegisterFunction;
import org.mgechev.distrelang.messages.Return;
import org.mgechev.distrelang.messages.SymbolTable;
import org.mgechev.elang.parser.expressions.IExpression;
import org.mgechev.elang.tokens.Token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Scheduler {
    
    private ArrayList<InetSocketAddress> hosts;
    private Map<String, Socket> mapper;
    private Map<InetSocketAddress, Socket> sockets;
    private int current = 0;
    
    public Scheduler(ArrayList<InetSocketAddress> hosts) {
        this.hosts = hosts;
        this.sockets = new HashMap<InetSocketAddress, Socket>();
        mapper = new HashMap<String, Socket>();
    }
    
    public IExpression invoke(String fun, ArrayList<IExpression> args) throws IOException {
        Socket socket = mapper.get(fun);
        if (socket.isClosed()) {
            return null;
        }
        Invoke msg = new Invoke();
        msg.name = fun;
        msg.args = args;
        this.send(msg, socket);
        Return res = (Return)this.read(socket, Return.class);
        return res.result;
    }
    
    public void register(ArrayList<Token> tokens) throws IOException {
        InetSocketAddress host = this.hosts.get(current % this.hosts.size());
        current += 1;
        Socket socket = this.sockets.get(host);
        if (socket == null) {
            socket = new Socket(host.getAddress(), host.getPort());
            this.sockets.put(host, socket);
        }
        RegisterFunction msg = new RegisterFunction();
        msg.tokens = tokens;
        this.send(msg, socket);
        RegisterComplete res = (RegisterComplete)this.read(socket, RegisterComplete.class);
        this.mapper.put(res.name, socket);
    }
    
    public void finalize() throws IOException {
        SymbolTable table = this.buildSymbolTable();
        for (InetSocketAddress addr : this.sockets.keySet()) {
            this.send(table, this.sockets.get(addr));
        }
    }
    
    private SymbolTable buildSymbolTable() {
        Map<String, InetSocketAddress> map = new HashMap<String, InetSocketAddress>();
        for (String fn : this.mapper.keySet()) {
            Socket socket = this.mapper.get(fn);
            InetSocketAddress addr = (InetSocketAddress)socket.getRemoteSocketAddress();
            map.put(fn, addr);
        }
        SymbolTable table = new SymbolTable();
        table.table = map;
        return table;
    }
    
    private void send(Message msg, Socket socket) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
        String toSend = gson.toJson(msg);
        OutputStream os = socket.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(toSend + "\n");
        writer.flush();
    }
    
    private Message read(Socket socket, Class type) throws IOException {
        InputStream is = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
        return gson.fromJson(reader.readLine(), type);
    }

}
