package org.mgechev.distrelang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.mgechev.distrelang.messages.Invoke;
import org.mgechev.distrelang.messages.Message;
import org.mgechev.distrelang.messages.RegisterFunction;
import org.mgechev.elang.parser.Parser;
import org.mgechev.elang.parser.expressions.symbols.functions.CustomFunction;
import org.mgechev.elang.parser.statements.IStatement;

import com.google.gson.Gson;
import com.sun.xml.internal.bind.CycleRecoverable.Context;

import sun.org.mozilla.javascript.internal.json.JsonParser;

public class Server {

    private int port;

    public Server(int port) {
        this.port = port;
    }
    
    public void invokeFunction(Invoke msg, Socket socket) {
        
    }
    
    public void listen() {
        try {
            final ServerSocket socket = new ServerSocket(this.port);
            final Server self = this;
            new Thread(new Runnable() {  
                public void run() {
                    try {
                        Socket client = socket.accept();
                        OutputStream os = client.getOutputStream();
                        InputStream is = client.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        Parser parser;
                        Gson gson = new Gson();
                        while (!socket.isClosed()) {
                            String line = reader.readLine();
                            Message fn = gson.fromJson(line, Message.class);
                            switch (fn.type) {
                            case INVOKE:
                                self.invokeFunction(gson.fromJson(line, Invoke.class), client);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }                    
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
