package org.mgechev.distrelang.messages;

public class RegisterComplete extends Message {
    public RemoteFunctionData data;
    public RegisterComplete() {
        this.type = MessageTypes.REGISTER_COMPLETE;
    }
}
