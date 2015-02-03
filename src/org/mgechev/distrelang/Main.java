package org.mgechev.distrelang;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.mgechev.elang.common.Program;

public class Main {

    public static void main(String[] args) throws IOException {
        
        if (args[0].equals("client")) {
            ArrayList<InetSocketAddress> hosts = new ArrayList<InetSocketAddress>();
            for (int i = 2; i < args.length; i += 1) {
                String host = args[i];
                String[] parts = host.split(":");
                hosts.add(new InetSocketAddress(Inet4Address.getByName(parts[0]), Integer.parseInt(parts[1])));
            }
            Scheduler scheduler = new Scheduler(hosts, new ConnectionProxy());
            DistrElang.loadProgramFile(args[1]);
            DistrElang.execute(scheduler, new Program());
        } else if (args[0].equals("server")) {
            int port = Integer.parseInt(args[1]);
            new Server(port, new ConnectionProxy(), new Program()).start();
        } else {
            throw new RuntimeException("The component is not recognized");
        }
    }
    
}
