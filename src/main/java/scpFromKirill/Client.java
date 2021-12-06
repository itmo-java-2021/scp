package scpFromKirill;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws IOException {
        String homeDir;
        String serverIP = "localhost";
        if (args.length != 0) {
            homeDir = args[0] + File.separator;
        } else {
            homeDir = "";
        }
        System.out.println("""
                Команды для работы:\s

                ls\t\t\t\t\t\t- вывести содержимое директории на сервера
                download [filename]\t\t- скачать файл с сервера
                upload [filename]\t\t- загрузить файл на сервер""");
        Scanner scan = new Scanner(System.in);
        while(true) {
            System.out.print("\n\nВведите команду: ");
            String readline = scan.nextLine();

            if (readline.length() != 0) {
                String[] command = readline.split(" ");
                StringBuilder commandReq;
                switch (command[0]) {
                    case "ls":
                        try (Socket socket = new Socket(serverIP, 42134);
                             ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                             ObjectInputStream is = new ObjectInputStream(socket.getInputStream())
                        ) {
                            commandReq = new StringBuilder("ls");
                            if (command.length > 1) {
                                commandReq.append(" ").append(command[1]);
                            }
                            String sendCommand = commandReq.toString();
                            os.writeObject(new Message(sendCommand, ""));
                            System.out.println(((Message) is.readObject()).getBody());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "download":
                        commandReq = new StringBuilder("download");
                        if (command.length > 1) {
                            commandReq.append(" ").append(command[1]);
                        } else {
                            System.err.println("Указаны неверные параметры для команды");
                            continue;
                        }
                        try (Socket socket = new Socket(serverIP, 42134);
                             ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                             ObjectInputStream is = new ObjectInputStream(socket.getInputStream())
                        ) {

                            String sendCommand = commandReq.toString();
                            os.writeObject(new Message(sendCommand, ""));
                            File file = new File(homeDir + command[1]);
                            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
                            CopyStream.copyStream(is, fos);
                            System.out.println("Загрузка завершена");
                        }
                        break;


                    case "upload":
                        commandReq = new StringBuilder("upload");
                        if (command.length > 1) {
                            commandReq.append(" ").append(command[1]);
                        } else {
                            System.err.println("Указаны неверные параметры для команды");
                            continue;
                        }
                        try (Socket socket = new Socket(serverIP, 42134);
                             ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream())
                        ) {
                            String sendCommand = commandReq.toString();
                            os.writeObject(new Message(sendCommand, ""));
                            File file = new File(homeDir + command[1]);
                            BufferedInputStream fos = new BufferedInputStream(new FileInputStream(file));
                            CopyStream.copyStream(fos, os);
                            System.out.println("Выгрузка завершена");
                        } catch (FileNotFoundException ex) {
                            System.err.println("Не найден указанный файл");
                        }
                        break;
                    default:
                        System.err.println("Введена неизвестная команда");
                }
            }
        }
    }
}
