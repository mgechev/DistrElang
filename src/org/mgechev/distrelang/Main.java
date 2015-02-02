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


//        ArrayList<InetSocketAddress> hosts = new ArrayList<InetSocketAddress>();
//        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55555));
//        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55554));
//        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55553));
//        Scheduler scheduler = new Scheduler(hosts, new ConnectionProxy());
//        
//        Server s1 = new Server(55555, new ConnectionProxy(), new Program());
//        s1.start();
//        Server s2 = new Server(55554, new ConnectionProxy(), new Program());
//        s2.start();
//        Server s3 = new Server(55553, new ConnectionProxy(), new Program());
//        s3.start();
//        
//        DistrElang.loadProgramString("def add(a, b)" +
//"  return a + b;" +
//"enddef;" +
//
//"def add2(a)" +
//"  return add(a, 2);" +
//"enddef;" +
//
//"def multi(a, b)" +
//"  return a * b;" +
//"enddef;" +
//
//"def complex(a, b)" +
//"  f = add2(a);" +
//"  return multi(f, b);" +
//"enddef;" +
//
//"print 'Add 2 to 40: ';" +
//"print complex(2, 3);" +
//"print '   ';");
//        
//        DistrElang.execute(scheduler, new Program());
    }
    
}
