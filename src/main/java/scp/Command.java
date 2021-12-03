package scp;

public class Command {
    public String command;
    public String paht1;
    public String path2;
    public Object data;

    public Command(String command) {
        var vs = command.split(" ");
        this.command = vs[0];
        if (vs.length == 3){
            this.paht1 = vs[1];
            this.path2 = vs[2];
        }
    }

    public Command(String command, Object data) {
        this(command);
        this.data = data;
    }
}
