package org.mgechev.distrelang;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.mgechev.distrelang.messages.Invoke;
import org.mgechev.distrelang.messages.Return;
import org.mgechev.elang.common.Program;
import org.mgechev.elang.parser.expressions.IExpression;
import org.mgechev.elang.parser.expressions.symbols.Value;
import org.mgechev.elang.parser.expressions.symbols.functions.CustomFunction;

public class RemoteFunction extends CustomFunction {
    private InetSocketAddress addr;
    private ConnectionProxy proxy;

    public RemoteFunction(InetSocketAddress addr, ConnectionProxy proxy, Program program) {
        this(addr, 0, proxy, program);
    }
    
    public RemoteFunction(InetSocketAddress addr, int count, ConnectionProxy proxy, Program program) {
        super(program, count);
        this.addr = addr;
        this.proxy = proxy;
    }

    @Override
    public Value evaluate() {
        Invoke msg = new Invoke();
        msg.name = this.name;
        msg.args = new ArrayList<Value>();
        for (Object expr : this.args) {
            msg.args.add(((IExpression)expr).evaluate());
        }
        try {
            proxy.send(msg, addr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Return res;
        try {
            res = (Return) proxy.read(addr, Return.class);
            return res.result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
