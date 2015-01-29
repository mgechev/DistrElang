package org.mgechev.distrelang.messages;

public class RegisterComplete extends Message {
    public String name;
    public RegisterComplete() {
        this.type = MessageTypes.REGISTER_COMPLETE;
    }
}
