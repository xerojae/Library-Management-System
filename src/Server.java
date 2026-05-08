import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    // Shared state for the 5-minute librarian registration code
    private static String currentLibrarianCode = "0000";
    private static long codeExpiryTime = 0;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        Database db = new Database(); 

        // Endpoint: Get the 5-minute code (Visible only to librarians in the UI)
        server.createContext("/api/librarian-code", (exchange) -> {
            long now = System.currentTimeMillis();
            if (now > codeExpiryTime) {
                currentLibrarianCode = String.format("%04d", new Random().nextInt(10000));
                codeExpiryTime = now + 300000; // Set expiry to 5 minutes (300,000 ms)
            }
            String response = "{\"code\":\"" + currentLibrarianCode + "\"}";
            sendResponse(exchange, response, 200);
        });

        // Endpoint: Get all books from books.txt
        server.createContext("/api/books", (exchange) -> {
            String json = "[" + db.getAllBooks().stream()
                .map(b -> String.format("{\"name\":\"%s\",\"author\":\"%s\",\"brwcopies\":%d}", 
                    b.getName(), b.getAuthor(), b.getBrwcopies()))
                .collect(Collectors.joining(",")) + "]";
            sendResponse(exchange, json, 200);
        });

        // Handler for the frontend website files
        server.createContext("/", new StaticFileHandler());

        System.out.println("Digital Library Server started on http://localhost:8080");
        server.start();
    }

    private static void sendResponse(HttpExchange exchange, String data, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, data.length());
        OutputStream os = exchange.getResponseBody();
        os.write(data.getBytes());
        os.close();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("frontend" + path);
            if (file.exists()) {
                t.sendResponseHeaders(200, file.length());
                OutputStream os = t.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                final byte[] buffer = new byte[0x10000];
                int count;
                while ((count = fs.read(buffer)) >= 0) os.write(buffer, 0, count);
                fs.close();
                os.close();
            } else {
                t.sendResponseHeaders(404, -1);
            }
        }
    }
}