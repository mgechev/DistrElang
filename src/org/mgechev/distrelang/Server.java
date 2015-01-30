package org.mgechev.distrelang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mgechev.distrelang.messages.Invoke;
import org.mgechev.distrelang.messages.Message;
import org.mgechev.distrelang.messages.RegisterComplete;
import org.mgechev.distrelang.messages.RegisterFunction;
import org.mgechev.distrelang.messages.Return;
import org.mgechev.distrelang.messages.SymbolTable;
import org.mgechev.elang.common.Program;
import org.mgechev.elang.parser.Parser;
import org.mgechev.elang.parser.expressions.symbols.Value;
import org.mgechev.elang.parser.expressions.symbols.functions.CustomFunction;
import org.mgechev.elang.parser.statements.IStatement;
import org.mgechev.elang.tokens.Token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.xml.internal.bind.CycleRecoverable.Context;

import sun.org.mozilla.javascript.internal.json.JsonParser;

public class Server extends Thread {

    private int port;

    public Server(int port) {
        this.port = port;
    }
    
    private void invokeFunction(Invoke msg, Socket socket) throws IOException {
        CustomFunction fn = Program.Get().getFunction(msg.name);
        fn.setArguments(msg.args);
        Value result = fn.evaluate();
        Return res = new Return();
        res.name = msg.name;
        res.result = result;
        this.send(res, socket);
    }
    
    private void registerFunction(RegisterFunction msg, Socket client) throws IOException {
        Object[] names = Program.Get().getFunctionsNames().toArray();
        Parser parser = new Parser(msg.tokens);
        parser.parse();
        Object[] newNames = Program.Get().getFunctionsNames().toArray();
        
        for (Object name : newNames) {
            boolean exists = false;
            for (Object oldName : names) {
                if (oldName.equals(name)) {
                    exists = true;
                }
            }
            if (!exists) {
                RegisterComplete response = new RegisterComplete();
                response.name = name.toString();
                System.out.println("Registered " + response.name);
                this.send(response, client);
                return;
            }
        }
    }
    
    private void send(Message msg, Socket socket) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
        String toSend = gson.toJson(msg);
        OutputStream os = socket.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(toSend + "\n");
        writer.flush();
    }
    
    private void saveSymbolTable(SymbolTable msg) {
        for (String fun : msg.table.keySet()) {
            RemoteFunction remoteFn = new RemoteFunction(msg.table.get(fun));
            Program.Get().addFunction(fun, remoteFn);
        }
    }
    
    public void run() {
        try {
            final ServerSocket socket = new ServerSocket(this.port);
            final Server self = this;
            Socket client = socket.accept();
            System.out.println("Client connected");
            OutputStream os = client.getOutputStream();
            InputStream is = client.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            Parser parser;
            Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
            while (!socket.isClosed()) {
                String line = reader.readLine();
                Message fn = gson.fromJson(line, Message.class);
                System.out.println("Message received " + line);
                switch (fn.type) {
                case INVOKE:
                    self.invokeFunction(gson.fromJson(line, Invoke.class), client);
                case REGISTER:
                    self.registerFunction(gson.fromJson(line, RegisterFunction.class), client);
                case SYMBOL_TABLE:
                    self.saveSymbolTable(gson.fromJson(line, SymbolTable.class));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }                            
    }
    
}
