package server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public class Main {
    public static void main(String[] args) throws IOException {
        //String address = "127.0.0.1";
        int port = 23456;
        mandavoshka("Server start");

        System.out.println("Server started!");
        try (ServerSocket server = new ServerSocket(port)) {

            mandavoshka("ServerSocket create");
            try (Socket socket = server.accept();
                 DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())
            ) {
                mandavoshka("got incoming socket from CLI");
                while (true) {
                    mandavoshka("start new loop");
                    String incomingCommand = input.readUTF();
                    mandavoshka("got incoming command");
                    System.out.println("Got order  " + incomingCommand);
                    if ("exit".equalsIgnoreCase(incomingCommand)) {
                        System.out.println("Shutdown this srvr");
                        mandavoshka("got exit comma");
                        break;
                    }
                    mandavoshka("not got EXIT");
                    ServerActions serverAction = switch (incomingCommand) {
                        case "GET" -> new SendFile();
                        case "PUT" -> new CreateFile();
                        case "DELETE" -> new DeleteFile();
                        default -> new DefaultAction();
                    };


                    serverAction.fileOperation(input, output);
                    System.out.println("Success file operation, back to main loop.");
                    mandavoshka("end loop");
                }

            }
        }

    }


    public static void mandavoshka(String s) {
        String fPuti = "D:\\ru.ngrv\\File Server\\File Server\\task\\src\\server\\data\\1488";
        File file = new File(fPuti);
        if (file.exists()) {
            try (FileWriter writer = new FileWriter(file,true)) {
                Date date = new Date();
                writer.write(date + " : " + s + "+1\n");
                writer.flush();
                } catch (IOException e) {
                        System.out.printf("Trouble with logfile : %s", e.getMessage());
                    }
        } else {
            System.out.println("PUTIN STOLE LOG FILE MFKA");
        }
    }

}

interface ServerActions {
    String FILE_STORAGE = "D:\\ru.ngrv\\File Server\\File Server\\task\\src\\server\\data\\";
    void fileOperation(DataInputStream input, DataOutputStream output) throws IOException;
}

class DefaultAction implements ServerActions {
    @Override
    public void fileOperation(DataInputStream input, DataOutputStream output) {
        System.out.println("Incorrect input command! Try again or type \"exit\" to close program...");
    }
}

class SendFile implements ServerActions {
    @Override
    public void fileOperation(DataInputStream input, DataOutputStream output) throws IOException {
        String requestedFileName = input.readUTF();
        System.out.println("Client request for " + requestedFileName);
        String filePath = FILE_STORAGE + requestedFileName;

        String responseStatus;
        String responseFileContent = "";

        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("File exists :" + file.getAbsolutePath());
            try {
                responseFileContent = new String(Files.readAllBytes(Paths.get(filePath)));
                responseStatus = "200";

            } catch (IOException e) {
                responseStatus = "403";
            }
        } else {
            responseStatus = "403";
        }

        output.writeUTF(responseStatus);
        if ("200".equalsIgnoreCase(responseStatus)) {
            output.writeUTF(responseFileContent);
        }

    }
}

class CreateFile implements ServerActions {
    @Override
    public void fileOperation(DataInputStream input, DataOutputStream output) throws IOException {
        String requestedFileName = input.readUTF();
        String filePath = FILE_STORAGE + requestedFileName;
        System.out.println("Preparing for write..." + filePath);
        String fileContent = input.readUTF();
        System.out.println("Got content for write to the file: " + fileContent);
        String responseStatus;

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Such file is not exist yet :" + file.getAbsolutePath());
            try {
                if (file.createNewFile()) {
                    System.out.println("File created : " + file.getName());
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(fileContent);
                    } catch (IOException e) {
                        System.out.printf("Trouble while writing file : %s", e.getMessage());
                    }
                    responseStatus = "200";
                } else {
                    System.out.println("Trouble witch creating file: " + file.getName());
                    responseStatus = "403";
                }
            } catch (IOException e) {
                responseStatus = "403";
            }
        } else {
            responseStatus = "403";
        }

        output.writeUTF(responseStatus);
        //try {Thread.sleep(100);} catch (InterruptedException e) {throw new RuntimeException(e);}
    }
}

class DeleteFile implements ServerActions {
    @Override
    public void fileOperation(DataInputStream input, DataOutputStream output) throws IOException {
        String requestedFileName = input.readUTF();
        String filePath = FILE_STORAGE + requestedFileName;

        String responseStatus;

        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                responseStatus = "200";

            } else {
                responseStatus = "403";
            }
        } else {
            responseStatus = "403";
        }

        output.writeUTF(responseStatus);
    }
}