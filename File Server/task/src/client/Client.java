package client;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        final String ADDRESS = "127.0.0.1";
        final int PORT = 23456;

        try {Thread.sleep(2000);} catch (InterruptedException e) {throw new RuntimeException(e);}

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command;

        try (Socket socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            while (true) {
                showUserMenu();
                command = reader.readLine().toLowerCase();

                if ("exit".equalsIgnoreCase(command)) {
                    output.writeUTF(command);
                    System.out.println("[Exit] command was sent.");
                    break;
                }

                ClientCommandsForServer fileOperation = switch (command) {
                    case "1" -> new GetFile();
                    case "2" -> new CreateFile();
                    case "3" -> new DeleteFile();
                    default -> new DefaultAction();
                };

                fileOperation.execute(reader, input, output);
                /// try {Thread.sleep(100);} catch (InterruptedException e) {throw new RuntimeException(e);}
            }
        }




    }

    static void showUserMenu() {
        System.out.println("Enter action (1 - get a file, 2 - create a file, 3 - delete a file):");
    }



}

interface ClientCommandsForServer {
    void execute(BufferedReader reader, DataInputStream input, DataOutputStream output) throws IOException;
}

class GetFile implements ClientCommandsForServer {

    @Override
    public void execute(BufferedReader reader, DataInputStream input, DataOutputStream output) throws IOException {
        output.writeUTF("GET");
        System.out.println("Enter filename: ");
        String fileName = reader.readLine();
        output.writeUTF(fileName);
        System.out.println("[GET] request was sent.");
        String serverStatus = input.readUTF();
        if ("200".equalsIgnoreCase(serverStatus)) {
            String serverResponse = input.readUTF();
            System.out.println("The content of the file is: " + serverResponse);
        } else {
            System.out.println("The response says that the file was not found!");
        }

    }
}

class CreateFile implements ClientCommandsForServer {

    @Override
    public void execute(BufferedReader reader, DataInputStream input, DataOutputStream output) throws IOException {
        System.out.println("Enter filename: ");
        String fileName = reader.readLine();
        System.out.println("Enter file content: ");
        String fileContent = reader.readLine();

        output.writeUTF("PUT");
        System.out.println("[PUT] request was sent.");
        //try {Thread.sleep(1000);} catch (InterruptedException e) {throw new RuntimeException(e);}

        output.writeUTF(fileName);
        //try {Thread.sleep(1000);} catch (InterruptedException e) {throw new RuntimeException(e);}

        output.writeUTF(fileContent);
        String serverStatus = input.readUTF();

        if ("200".equalsIgnoreCase(serverStatus)) {
            System.out.println("The response says that file was created!");
        } else {
            System.out.println("The response says that something going wrong!");
        }


    }

}

class DeleteFile implements ClientCommandsForServer {

    @Override
    public void execute(BufferedReader reader, DataInputStream input, DataOutputStream output) throws IOException {
        output.writeUTF("DELETE");
        System.out.println("Enter filename: ");
        String fileName = reader.readLine();
        output.writeUTF(fileName);
        System.out.println("[DELETE] request was sent.");
        String serverStatus = input.readUTF();
        if ("200".equalsIgnoreCase(serverStatus)) {
            System.out.println("The response says that the file was successfully deleted!");
        } else {
            System.out.println("The response says that the file was not found!");
        }
    }

}

class DefaultAction implements ClientCommandsForServer {
    @Override
    public void execute(BufferedReader reader, DataInputStream input, DataOutputStream output) {
        System.out.println("Incorrect input command! Try again or type \"exit\" to close program...");
    }
}