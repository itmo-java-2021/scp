package scp;

import scp.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements AutoCloseable {
    public static void main(String[] args) throws Exception {
        try (Server server = new Server(12345)) {
            server.start();
            Waite waite = new Waite();
            waite.waite();
        }
    }

    private static class Waite{

        public void waite() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = scanner.nextLine();

                String[] commandArgs = command.split("\s");
                switch (commandArgs[0]) {
                    case "quit":
                        System.out.println("Bye");
                        return;
                    default:
                        System.err.println("Неизвестная команда: " + commandArgs[0]);
                        break;
                }
            }
        }
    }



    private final int port;
    private final List<ConnectionServer> connections = new ArrayList<>();
    private ConnectionListener listener;
    private final String defaultPath = "c:\\Users\\U\\Documents\\java\\testSCP\\server\\";

    public Server(int port) {
        this.port = port;
    }

    public synchronized void start() {
        var listener = new ConnectionListener();
        listener.start();
        this.listener = listener;
    }

    public synchronized void stop() throws InterruptedException {
        if (listener != null) {
            IOUtils.closeQuietly(listener);
            listener = null;
        }

        // Выполняем копию, т.к. каждый остановленный поток автоматически удаляет себя из списка connections,
        // а мы хотим выполнить join к каждому, чтобы дождаться, пока завершатся все потоки.
        final var copyConnections = new ArrayList<>(connections);
        copyConnections.forEach(IOUtils::closeQuietly);
        for (ConnectionServer con : copyConnections) {
            con.join();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    private class ConnectionListener extends Thread implements AutoCloseable {
        private ServerSocket ssocket;

        @Override
        public void run() {
            try (ServerSocket ssocket = new ServerSocket(port)) {
                this.ssocket = ssocket;

                while (!isInterrupted()) {
                    final Socket socket = ssocket.accept();

                    final ConnectionServer connectionServer = new ConnectionServer(socket);
                    synchronized (connections) {
                        connections.add(connectionServer);
                    }
                    connectionServer.start();
                }
            } catch (IOException e) {
                // Можем пропустить вывод ошибки в консоль, если мы знаем, что это был останов.
                if (!isInterrupted()) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void close() throws Exception {
            interrupt();
            // Нужно отдельно закрывать сокет, т.к. метод accept() не выбрасывает InterruptedException,
            // а значит поток из него не выйдет по вызову метода interrupt();
            if (ssocket != null) {
                ssocket.close();
            }
        }
    }

    private class ConnectionServer extends Thread implements AutoCloseable {
        private final Socket socket;

        private ConnectionServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (socket;
                final var objIn = new ObjectInputStream(socket.getInputStream());
                final var objOut = new ObjectOutputStream(socket.getOutputStream())) {
                while (!isInterrupted()) {
                    final Command command = (Command) objIn.readObject();
                    switch (command.command){
                        case "ls":
                            File folder = new File(defaultPath);
                            File[] listOfFiles = folder.listFiles();
                            Command command1 = new Command("response", listOfFiles);
                            objOut.writeObject(command1);
                            break;
                    }
                }

            } catch (IOException e) {
                System.err.println("Client " + socket.getInetAddress() + " disconnected");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                synchronized (connections) {
                    connections.remove(this);
                }
            }
        }

        @Override
        public void close() throws Exception {
            interrupt();
            // Аналогично с ServerSocket, метод read не завершится по вызову interrupt().
            if (socket != null) {
                socket.close();
            }
        }
    }
}
