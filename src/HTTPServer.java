import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class HTTPServer {
    private final int port;
    private final String directory;

    public HTTPServer(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(port) ) {
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Client connected success");
                Thread thread = new Handler(socket, directory);
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new HTTPServer(8000, "D:\\Study\\6 сем\\АиПОСиЗИ\\lab1\\files").start();
    }
}