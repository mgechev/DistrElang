package org.mgechev.distrelang;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.mgechev.distrelang.messages.Invoke;
import org.mgechev.distrelang.messages.Return;
import org.mgechev.elang.parser.expressions.IExpression;
import org.mgechev.elang.parser.expressions.symbols.Value;
import org.mgechev.elang.parser.expressions.symbols.functions.CustomFunction;

public class RemoteFunction extends CustomFunction {
    private InetSocketAddress addr;

    public RemoteFunction(InetSocketAddress addr) {
        this(addr, 0);
    }
    
    public RemoteFunction(InetSocketAddress addr, int count) {
        super(count);
        this.addr = addr;
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
            ConnectionProxy.Get().send(msg, addr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Return res;
        try {
            res = (Return) ConnectionProxy.Get().read(addr, Return.class);
            return res.result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
