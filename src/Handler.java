import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

public class Handler extends Thread {

    private static final HashMap<String, String> CONTENT_TYPES = new HashMap<>() {{
        put("jpg", "image/jpeg");
        put("png", "image/png");
        put("html", "text/html");
        put("txt", "text/plain");
        put("", "text/plain");
    }};

    private static final String NOT_FOUND_MESSAGE = "NOT FOUND";
    private static final String MAIN_PAGE = "MAIN PAGE";
    private final Socket socket;
    private final String directory;

    public Handler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream(); OutputStream outputStream = socket.getOutputStream()) {
            String url = getRequestUrl(input);
            Path filePath = Path.of(directory + url);
            System.out.println(filePath.toString());

            if (filePath.toString().equals(directory)) {
                var type = CONTENT_TYPES.get("text");
                sendHandler(outputStream, 200, MAIN_PAGE, type, MAIN_PAGE.length());
                outputStream.write(MAIN_PAGE.getBytes(StandardCharsets.UTF_8));
            } else if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String extension = getFileExtension(filePath);
                String type = CONTENT_TYPES.get(extension);
                var fileBytes = Files.readAllBytes(filePath);
                sendHandler(outputStream, 200, "OK", type, fileBytes.length);
                outputStream.write(fileBytes);
            } else {
                var type = CONTENT_TYPES.get("text");
                sendHandler(outputStream, 404, NOT_FOUND_MESSAGE, type, NOT_FOUND_MESSAGE.length());
                outputStream.write(NOT_FOUND_MESSAGE.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileExtension(Path path) {
        String name = path.getFileName().toString();
        int extensionStart = name.lastIndexOf(".");
        return extensionStart == -1 ? "" : name.substring(extensionStart + 1);
    }

    private String getRequestUrl(InputStream input) {
        String line;
        Scanner reader = new Scanner(input).useDelimiter("\r\n");
        line = reader.next();
        System.out.println();
        System.out.println(line);
        return line.split(" ")[1];

    }

    private void sendHandler(OutputStream outputStream, int statusCode, String statusText, String type, long length) {
        PrintStream printStream = new PrintStream(outputStream);
        printStream.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        printStream.printf("Content-Type: %s%n", type);
        printStream.printf("Content-Length: %s%n%n", length);
    }
}
