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
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mgechev.distrelang.messages.Message;
import org.mgechev.distrelang.messages.MessageTypes;
import org.mgechev.elang.tokens.Token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConnectionProxy {

    private Map<InetSocketAddress, Socket> cache;
    
    public ConnectionProxy() {
        synchronized (this) {
            cache = new HashMap<InetSocketAddress, Socket>();
        }
    }
    
    public synchronized Socket getSocket(InetSocketAddress addr) throws UnknownHostException, IOException {
        Socket s = cache.get(addr);
        if (s != null) {
            return s;
        }
        s = new Socket(addr.getHostName(), addr.getPort());
        cache.put(addr, s);
        return s;
    }
    
    public synchronized Collection<Socket> getSockets() {
        return this.cache.values();
    }
    
    public synchronized void send(Message msg, InetSocketAddress addr) throws IOException {
        Socket socket = this.getSocket(addr);
        Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
        String toSend = gson.toJson(msg);
        OutputStream os = socket.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(toSend + "\n");
        writer.flush();
    }
    
    public synchronized Message read(InetSocketAddress addr, Class type) throws IOException {
        Socket socket = this.getSocket(addr);
        InputStream is = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Gson gson = new GsonBuilder().registerTypeAdapter(Token.class, new InterfaceAdapter<Token>()).create();
        return gson.fromJson(reader.readLine(), type);
    }
    
}
