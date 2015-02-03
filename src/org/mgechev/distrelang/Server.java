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
import java.util.ArrayList;
import java.util.List;

import org.mgechev.distrelang.messages.Host;
import org.mgechev.distrelang.messages.Invoke;
import org.mgechev.distrelang.messages.Message;
import org.mgechev.distrelang.messages.RegisterComplete;
import org.mgechev.distrelang.messages.RegisterFunction;
import org.mgechev.distrelang.messages.RemoteFunctionData;
import org.mgechev.distrelang.messages.Return;
import org.mgechev.distrelang.messages.SymbolTable;
import org.mgechev.elang.common.Program;
import org.mgechev.elang.parser.Parser;
import org.mgechev.elang.parser.expressions.symbols.Function;
import org.mgechev.elang.parser.expressions.symbols.Value;
import org.mgechev.elang.parser.expressions.symbols.functions.CustomFunction;
import org.mgechev.elang.tokens.Token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Server extends Thread {

    private Program program;
    private int port;
    private ConnectionProxy proxy;
    // Required because we don't know all the functions' names in the
    // moment of parsing the program
    private List<ArrayList<Token>> buffer;
    private List<ClientHandler> handlers;

    public Server(int port, ConnectionProxy proxy, Program program) {
        this.port = port;
        this.proxy = proxy;
        this.buffer = new ArrayList<ArrayList<Token>>();
        this.handlers = new ArrayList<Server.ClientHandler>();
        this.program = program;
    }
    
    private synchronized void invokeFunction(Invoke msg, Socket socket) throws IOException {
        CustomFunction fn = program.getFunction(msg.name);
        for (Value val : msg.args) {
            fn.setOperand(val);
        }
        Value result = fn.evaluate();
        Return res = new Return();
        res.name = msg.name;
        res.result = result;
        this.send(res, socket);
    }
    
    private synchronized void registerFunction(RegisterFunction msg, Socket client) throws IOException {
        Object[] names = program.getFunctionsNames().toArray();
        buffer.add(msg.tokens);
        Parser parser = new Parser(msg.tokens, program);
        parser.parse();
        Object[] newNames = program.getFunctionsNames().toArray();
        
        for (Object name : newNames) {
            boolean exists = false;
            for (Object oldName : names) {
                if (oldName.equals(name)) {
                    exists = true;
                }
            }
            if (!exists) {
                RegisterComplete response = new RegisterComplete();
                Function fn = program.getFunction(name.toString());
                RemoteFunctionData data = new RemoteFunctionData();
                data.name = name.toString();
                data.argsCount = fn.getArgumentsCount();
                response.data = data;
                this.send(response, client);
                return;
            }
        }
    }
    
    private synchronized void send(Message msg, Socket socket) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
        String toSend = gson.toJson(msg);
        OutputStream os = socket.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(toSend + "\n");
        writer.flush();
    }
    
    private synchronized void saveSymbolTable(SymbolTable msg) {
        for (String fun : msg.table.keySet()) {
            Host host = msg.table.get(fun);
            RemoteFunction remoteFn = new RemoteFunction(new InetSocketAddress(host.hostname, host.port), msg.args.get(fun), this.proxy, program);
            remoteFn.setName(fun);
            try {
                program.getFunction(fun);
            } catch (Exception e) {
                program.addFunction(fun, remoteFn);
            }
        }
        for (ArrayList<Token> tokens : this.buffer) {
            Parser p = new Parser(tokens, program);
            p.parse();
        }
    }
    
    public class ClientHandler extends Thread {
        private Socket socket;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
                while (!socket.isClosed()) {
                    String line = reader.readLine();
                    Message msg = gson.fromJson(line, Message.class);
                    if (msg == null) {
                        socket.close();
                        return;
                    }
                    switch (msg.type) {
                    case INVOKE:
                        Server.this.invokeFunction(gson.fromJson(line, Invoke.class), socket);
                        break;
                    case REGISTER:
                        Server.this.registerFunction(gson.fromJson(line, RegisterFunction.class), socket);
                        break;
                    case SYMBOL_TABLE:
                        Server.this.saveSymbolTable(gson.fromJson(line, SymbolTable.class));
                        break;
                    default:
                        break;
                    }
                }
                socket.close();
            } catch (IOException e) {
                
            }
        }
    }
    
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(this.port);
            Socket client;
            while ((client = socket.accept()) != null) {
                ClientHandler handler = new ClientHandler(client);
                this.handlers.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
