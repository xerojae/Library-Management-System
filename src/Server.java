import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    private static String currentLibrarianCode = "0000";
    private static long codeExpiryTime = 0;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        Database db = new Database(); 

        // API for the 5-minute registration code
        server.createContext("/api/librarian-code", (exchange) -> {
            long now = System.currentTimeMillis();
            if (now > codeExpiryTime) {
                currentLibrarianCode = String.format("%04d", new Random().nextInt(10000));
                codeExpiryTime = now + 300000; 
            }
            String response = "{\"code\":\"" + currentLibrarianCode + "\"}";
            sendResponse(exchange, response, 200, "application/json");
        });

        // API for book data
        server.createContext("/api/books", (exchange) -> {
            String json = "[" + db.getAllBooks().stream()
                .map(obj -> (Book) obj) 
                .map(b -> String.format("{\"name\":\"%s\",\"author\":\"%s\",\"brwcopies\":%d}", 
                    b.getName(), b.getAuthor(), b.getBrwCopies()))
                .collect(Collectors.joining(",")) + "]";
            sendResponse(exchange, json, 200, "application/json");
        });

        server.createContext("/", new StaticFileHandler());
        System.out.println("System online at http://localhost:8080");
        server.start();
    }

    private static void sendResponse(HttpExchange exchange, String data, int status, String type) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, data.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(data.getBytes());
        os.close();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            // Ensure folder name matches your directory (Frontend vs frontend)
            File file = new File("Frontend" + path);
            
            if (file.exists()) {
                String contentType = "text/plain";
                if (path.endsWith(".html")) contentType = "text/html";
                else if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "text/javascript";

                t.getResponseHeaders().set("Content-Type", contentType);
                t.sendResponseHeaders(200, file.length());
                
                OutputStream os = t.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                byte[] buffer = new byte[0x10000];
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