package org.mgechev.distrelang;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        ArrayList<InetSocketAddress> hosts = new ArrayList<InetSocketAddress>();
        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55555));
        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55554));
        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55553));
        Scheduler scheduler = new Scheduler(hosts, new ConnectionProxy());
        
        Server s1 = new Server(55555, new ConnectionProxy());
        s1.start();
        Server s2 = new Server(55554, new ConnectionProxy());
        s2.start();
        Server s3 = new Server(55553, new ConnectionProxy());
        s3.start();
        
        DistrElang.loadProgramString("def sum(a, b)"
                + "return a + b;"
                + "enddef;"
                + "def sum2(a)"
                + "return sum(a, 2);"
                + "enddef;"
                + "def multi(a, b)"
                + "return a * b;"
                + "enddef;"
                + "def complex(a, b)"
                + "temp = sum2(a);"
                + "return multi(a, b);"
                + "enddef;"
                //+ "print sum(1, 2);"
                + "print '\n';"
                //+ "print sum2(3);"
                + "print '\n';"
                //+ "print multi(8, 8);"
                + "print '\n';"
                + "print complex(2, 3);"
                + "print '\n';");
        DistrElang.execute(scheduler);
    }
    
}
