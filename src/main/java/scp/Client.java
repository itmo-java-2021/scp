package scp;


import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client implements Closeable {
    private static final String HELP_STRING = """
            Команды:
            ls
            quit
            help
            """;

    private final String host;
    private final int port;
    private Socket socket;
    private ObjectInputStream objIn;
    private ObjectOutputStream objOut;

    public Client(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        socket = new Socket(host, port);
        objOut = new ObjectOutputStream(socket.getOutputStream());
        objIn = new ObjectInputStream(socket.getInputStream());
    }


    public static void main(String[] args) throws Exception {
        try (Client client = new Client("127.0.0.1", 12345)) {
            client.start();
        }
    }

    public void start() {
        help();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (isBlank(command)) {
                System.err.println("Команды не указана");
                help();

                continue;
            }

            String[] commandArgs = command.split("\s");
            switch (commandArgs[0]) {
                case "ls":
                    ls();
                    break;
                case "quit":
                    System.out.println("Bye");
                    return;
                default:
                    System.err.println("Неизвестная команда: " + commandArgs[0]);
                    help();
                    break;
            }
        }
    }

    private void ls(){
        try {
            Command command = new Command("ls", 0);
            objOut.writeObject(command);
            final Command response = (Command) objIn.readObject();
            File[] res = (File[])response.data;
            for (File file : res) {
                System.out.println(file.getName());
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void help() {
        System.out.println(HELP_STRING);
    }

    private boolean isBlank(String string) {
        return string == null || string.isBlank();
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
}
