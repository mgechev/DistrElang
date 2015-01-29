package org.mgechev.distrelang;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.mgechev.elang.ELang;

public class Main {

    public static void main(String[] args) throws UnknownHostException {
        ArrayList<InetSocketAddress> hosts = new ArrayList<InetSocketAddress>();
        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55555));
        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55554));
        hosts.add(new InetSocketAddress(Inet4Address.getByName("127.0.0.1"), 55553));
        Scheduler scheduler = new Scheduler(hosts);
        
        ELang.loadProgramFile(args[0]);
        ELang.execute(scheduler);
    }
    
}
