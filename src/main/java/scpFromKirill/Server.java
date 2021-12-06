package scpFromKirill;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class Server {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        StringBuilder homeDir = new StringBuilder();
        if (args.length != 0) {
            homeDir.append(args[0]).append(File.separator);
        } else {
            homeDir.append("D:\\");
        }

        ServerSocket ss = new ServerSocket(42134);
        while (true) {
            StringBuilder homePlusFileDir = new StringBuilder(homeDir);
            String filename;
            try (Socket socket = ss.accept();
                 ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream())) {

                Message messageRead = (Message) is.readObject();
                String[] command = messageRead.getRequest().split(" ");
                switch (command[0]) {
                    case "ls" -> {
                        if (command.length > 1) {
                            homePlusFileDir.append(command[1]);
                        }
                        Process run = Runtime.getRuntime().exec("cmd /C dir " + homePlusFileDir);
                        BufferedReader bufRead = new BufferedReader(new InputStreamReader(run.getInputStream(), Charset.forName("cp866")));
                        String line;
                        StringBuilder strBuil = new StringBuilder();
                        while ((line = bufRead.readLine()) != null) {
                            strBuil.append(line).append("\n");
                        }
                        Message message = new Message("ls", strBuil.toString());
                        os.writeObject(message);
                    }
                    case "download" -> {
                        filename = command[1];
                        File downloadFile = new File(homeDir + filename);
                        try (BufferedInputStream bufInStr = new BufferedInputStream(new FileInputStream(downloadFile))) {
                            CopyStream.copyStream(bufInStr, os);
                        } catch (FileNotFoundException ex) {
                            System.err.println("Файл не найден");
                        }
                    }
                    case "upload" -> {
                        filename = command[1];
                        File uploadFile = new File(homeDir + filename);
                        try (BufferedOutputStream bufOutStr = new BufferedOutputStream(new FileOutputStream(uploadFile))) {
                            CopyStream.copyStream(is, bufOutStr);
                        } catch (FileNotFoundException ex) {
                            System.err.println("Файл не найден");
                        }
                    }
                }
            }
        }
    }
}
