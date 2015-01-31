package org.mgechev.distrelang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
import org.mgechev.elang.parser.expressions.symbols.Variable;
import org.mgechev.elang.parser.expressions.symbols.functions.CustomFunction;
import org.mgechev.elang.tokens.Token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Server extends Thread {

    private int port;
    private ConnectionProxy proxy;

    public Server(int port, ConnectionProxy proxy) {
        this.port = port;
        this.proxy = proxy;
    }
    
    private void invokeFunction(Invoke msg, Socket socket) throws IOException {
        CustomFunction fn = Program.Get().getFunction(msg.name);
        if (msg.name.equals("complex")) {
            System.out.print("TEST");
        }
        for (Value val : msg.args) {
            fn.setOperand(val);
        }
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
                Function fn = Program.Get().getFunction(name.toString());
                RemoteFunctionData data = new RemoteFunctionData();
                data.name = name.toString();
                data.argsCount = fn.getArgumentsCount();
                response.data = data;
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
            RemoteFunction remoteFn = new RemoteFunction(msg.table.get(fun), msg.args.get(fun), this.proxy);
            if (Program.Get().getFunction(fun) == null) {
                Program.Get().addFunction(fun, remoteFn);
            }
        }
    }
    
    public void run() {
        try {
            ServerSocket socket = new ServerSocket(this.port);
            Socket client = socket.accept();
            InputStream is = client.getInputStream();
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
                    this.invokeFunction(gson.fromJson(line, Invoke.class), client);
                    break;
                case REGISTER:
                    this.registerFunction(gson.fromJson(line, RegisterFunction.class), client);
                    break;
                case SYMBOL_TABLE:
                    this.saveSymbolTable(gson.fromJson(line, SymbolTable.class));
                    break;
                default:
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
