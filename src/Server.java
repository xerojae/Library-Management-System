import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class Server {
    private static Database db = new Database(); 
    private static String currentLibrarianCode = "0000";
    private static long codeExpiryTime = 0;
    private static List<String> auditLog = new ArrayList<>(Arrays.asList("System initialized."));
    
    // List to hold active support tickets in memory
    private static List<String> supportTickets = new ArrayList<>();

    // NEW: Map to hold dynamic curated lists (Course Name -> List of Keywords/Books)
    private static Map<String, List<String>> curatedLists = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        // Initialize default curated lists
        curatedLists.put("COSC 369: Software Engineering", new ArrayList<>(Arrays.asList("Frontend", "Testing", "Digital Library", "Software")));
        curatedLists.put("ENG 101: Intro to Literature", new ArrayList<>(Arrays.asList("Robbit", "Toads", "Hunger Games")));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // AUTHENTICATION ENDPOINTS

        server.createContext("/api/register", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String[] parts = readBody(exchange).split("\\|");
                String email = parts[0];
                String pass = parts[1];
                String role = parts[2];

                if (db.userExist(email)) {
                    sendResponse(exchange, "{\"message\":\"Account already exists!\"}", 400, "application/json");
                    return;
                }

                User newUser;
                if ("librarian".equals(role)) {
                    newUser = new Admin(email, pass, email); 
                } else {
                    newUser = new NormalUser(email, pass, email);
                }
                
                db.AddUser(newUser);
                int userIndex = db.login(pass, email);
                
                auditLog.add(0, "System: New " + role + " registered (" + email + ")");
                sendResponse(exchange, "{\"message\":\"Registered successfully\", \"userIndex\":" + userIndex + "}", 200, "application/json");
            }
        });

        server.createContext("/api/login", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String[] parts = readBody(exchange).split("\\|");
                String email = parts[0];
                String pass = parts[1];

                int userIndex = db.login(pass, email); 
                if (userIndex != -1) {
                    User u = db.getUser(userIndex);
                    String role = (u instanceof Admin) ? "librarian" : "student";
                    sendResponse(exchange, "{\"role\":\"" + role + "\", \"userIndex\":" + userIndex + "}", 200, "application/json");
                } else {
                    sendResponse(exchange, "{\"message\":\"Invalid email or password!\"}", 401, "application/json");
                }
            }
        });

        // GET /api/books 
        server.createContext("/api/books", (exchange) -> {
            String json = "[" + db.getAllBooks().stream()
                .map(obj -> (Book) obj)
                .map(b -> String.format("{\"name\":\"%s\",\"author\":\"%s\",\"publisher\":\"%s\",\"address\":\"%s\",\"qty\":%d,\"price\":%.2f,\"brwcopies\":%d}", 
                    b.getName().replace("\"", "\\\""), b.getAuthor().replace("\"", "\\\""), b.getPublisher(), b.getAddress(), b.getQTY(), b.getPrice(), b.getBrwCopies()))
                .collect(Collectors.joining(",")) + "]";
            sendResponse(exchange, json, 200, "application/json");
        });

        // GET /api/search?q=title 
        server.createContext("/api/search", (exchange) -> {
            String query = getQueryParam(exchange, "q").toLowerCase();
            String json = "[" + db.getAllBooks().stream()
                .map(obj -> (Book) obj)
                .filter(b -> b.getName().toLowerCase().contains(query) || b.getAuthor().toLowerCase().contains(query))
                .map(b -> String.format("{\"name\":\"%s\",\"author\":\"%s\",\"publisher\":\"%s\",\"address\":\"%s\",\"qty\":%d,\"price\":%.2f,\"brwcopies\":%d}", 
                    b.getName().replace("\"", "\\\""), b.getAuthor().replace("\"", "\\\""), b.getPublisher(), b.getAddress(), b.getQTY(), b.getPrice(), b.getBrwCopies()))
                .collect(Collectors.joining(",")) + "]";
            sendResponse(exchange, json, 200, "application/json");
        });

        // USER ENDPOINTS

        server.createContext("/api/borrow", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String[] parts = readBody(exchange).split("\\|");
                if (parts.length < 2) {
                    sendResponse(exchange, "{\"message\":\"Missing data.\"}", 400, "application/json");
                    return;
                }
                
                int userIndex = Integer.parseInt(parts[0]);
                String bookName = parts[1];
                
                User u;
                try { 
                    u = db.getUser(userIndex); 
                } catch (Exception e) { 
                    sendResponse(exchange, "{\"message\":\"Session invalid. Relogin.\"}", 401, "application/json"); 
                    return; 
                }

                int bookIndex = db.getBook(bookName);
                
                if (bookIndex != -1) {
                    Book b = db.getBook(bookIndex);
                    if (b.getBrwCopies() > 0) {
                        b.setBrwCopies(b.getBrwCopies() - 1);
                        
                        LocalDate start = LocalDate.now();
                        LocalDate finish = start.plusDays(14);
                        Borrowing brw = new Borrowing(start, finish, b, u);
                        db.borrowBook(brw, b, bookIndex);

                        auditLog.add(0, u.getName() + " borrowed: " + bookName);
                        sendResponse(exchange, "{\"message\":\"Book borrowed successfully.\"}", 200, "application/json");
                        return;
                    }
                }
                sendResponse(exchange, "{\"message\":\"Could not borrow book.\"}", 400, "application/json");
            }
        });

        server.createContext("/api/my-borrowings", (exchange) -> {
            String userIndexStr = getQueryParam(exchange, "userIndex");
            if (userIndexStr.isEmpty()) {
                sendResponse(exchange, "[]", 200, "application/json");
                return;
            }
            
            int userIndex = Integer.parseInt(userIndexStr);
            User u;
            try { 
                u = db.getUser(userIndex); 
            } catch (Exception e) { 
                sendResponse(exchange, "[]", 200, "application/json"); 
                return; 
            }
            
            String userName = u.getName();
            List<String> userBooks = new ArrayList<>();
            
            for (Borrowing brw : db.getAllBorrowings()) {
                String brwStr = brw.toString2();
                if (brwStr.contains(userName)) {
                    for (Book b : db.getAllBooks()) {
                        if (brwStr.contains(b.getName())) {
                            userBooks.add("\"" + b.getName().replace("\"", "\\\"") + "\"");
                            break;
                        }
                    }
                }
            }
            
            String json = "[" + String.join(",", userBooks) + "]";
            sendResponse(exchange, json, 200, "application/json");
        });

        server.createContext("/api/calculate-fine", (exchange) -> {
            String userIndexStr = getQueryParam(exchange, "userIndex");
            if (userIndexStr.isEmpty()) {
                sendResponse(exchange, "{\"fine\": 0.00}", 200, "application/json");
                return;
            }
            
            try {
                int userIndex = Integer.parseInt(userIndexStr);
                User u = db.getUser(userIndex);
                
                double totalFine = 0.00;
                double finePerDay = 0.50; 
                LocalDate today = LocalDate.now();

                for (Borrowing brw : db.getAllBorrowings()) {
                    if (brw.getUser().getName().equals(u.getName())) {
                        LocalDate dueDate = LocalDate.parse(brw.getFinish(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        if (today.isAfter(dueDate)) {
                            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                            totalFine += daysLate * finePerDay;
                        }
                    }
                }
                
                sendResponse(exchange, String.format("{\"fine\": %.2f}", totalFine), 200, "application/json");
                
            } catch (Exception e) {
                sendResponse(exchange, "{\"fine\": 0.00}", 400, "application/json");
            }
        });

        server.createContext("/api/return-book", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String[] parts = readBody(exchange).split("\\|");
                if (parts.length < 2) {
                    sendResponse(exchange, "{\"message\":\"Missing data.\"}", 400, "application/json");
                    return;
                }
                
                int userIndex = Integer.parseInt(parts[0]);
                String bookName = parts[1];

                User u;
                try { 
                    u = db.getUser(userIndex); 
                } catch (Exception e) { 
                    sendResponse(exchange, "{\"message\":\"Session invalid. Relogin.\"}", 401, "application/json"); 
                    return; 
                }

                int bookIndex = db.getBook(bookName);
                
                if (bookIndex != -1) {
                    Book b = db.getBook(bookIndex);
                    Borrowing toRemove = null;
                    
                    for (Borrowing brw : db.getAllBorrowings()) {
                        String brwStr = brw.toString2();
                        if (brwStr.contains(bookName) && brwStr.contains(u.getName())) {
                            toRemove = brw;
                            break;
                        }
                    }
                    
                    if (toRemove != null) {
                        b.setBrwCopies(b.getBrwCopies() + 1);
                        db.returnBook(toRemove, b, bookIndex);
                        auditLog.add(0, u.getName() + " returned: " + bookName);
                        sendResponse(exchange, "{\"message\":\"Book returned successfully.\"}", 200, "application/json");
                        return;
                    }
                }
                sendResponse(exchange, "{\"message\":\"Borrowing record not found.\"}", 400, "application/json");
            }
        });

        // LIBRARIAN ENDPOINTS

        server.createContext("/api/librarian-code", (exchange) -> {
            String force = getQueryParam(exchange, "force");
            if ("true".equals(force)) {
                currentLibrarianCode = String.format("%04d", new Random().nextInt(10000));
                codeExpiryTime = System.currentTimeMillis() + 300000;
            } else {
                updateCode();
            }
            sendResponse(exchange, "{\"code\":\"" + currentLibrarianCode + "\"}", 200, "application/json");
        });

        server.createContext("/api/add-book", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String data = readBody(exchange);
                String[] parts = data.split("\\|");
                
                String title = parts[0];
                String author = (parts.length > 1) ? parts[1] : "Unknown Author";
                
                int qty = 1;
                try {
                    if (parts.length > 2) qty = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing quantity for new book.");
                }

                Book nb = new Book();
                nb.setName(title);
                nb.setAuthor(author);
                nb.setPublisher("N/A");
                nb.setAddress("N/A");
                nb.setPrice(0.0);
                nb.setQTY(qty);               
                nb.setBrwCopies(qty);         
                
                db.addBook(nb); 
                
                auditLog.add(0, "Librarian added resource: " + title + " (Qty: " + qty + ")");
                sendResponse(exchange, "{\"message\":\"Book added to catalog.\"}", 200, "application/json");
            }
        });

        server.createContext("/api/delete-book", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String title = readBody(exchange);
                db.deleteBook(title); 
                
                auditLog.add(0, "Librarian deleted: " + title);
                sendResponse(exchange, "{\"message\":\"Book removed from catalog.\"}", 200, "application/json");
            }
        });

        server.createContext("/api/delete-all", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                db.deleteAllData(); 
                auditLog.add(0, "CRITICAL: Librarian cleared all database data.");
                sendResponse(exchange, "{\"message\":\"All data deleted.\"}", 200, "application/json");
            }
        });

        server.createContext("/api/view-borrowings", (exchange) -> {
            sendResponse(exchange, "{\"borrowings\": []}", 200, "application/json");
        });

        server.createContext("/api/view-orders", (exchange) -> {
            sendResponse(exchange, "{\"orders\": []}", 200, "application/json");
        });

        server.createContext("/api/audit-log", (exchange) -> {
            String json = "[" + auditLog.stream().map(s -> "\"" + s.replace("\"", "\\\"") + "\"").collect(Collectors.joining(",")) + "]";
            sendResponse(exchange, json, 200, "application/json");
        });

        // SUPPORT TICKET ENDPOINTS
        server.createContext("/api/submit-ticket", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String data = readBody(exchange);
                String[] parts = data.split("\\|\\|");
                if (parts.length >= 3) {
                    String userIndexStr = parts[0];
                    String subject = parts[1];
                    String message = parts[2];
                    
                    String userName = "Unknown Student";
                    try {
                        User u = db.getUser(Integer.parseInt(userIndexStr));
                        userName = u.getName();
                    } catch (Exception e) {}
                    
                    supportTickets.add(userName + "||" + subject + "||" + message);
                    auditLog.add(0, "New support ticket submitted by: " + userName);
                    
                    sendResponse(exchange, "{\"message\":\"Ticket submitted successfully.\"}", 200, "application/json");
                } else {
                    sendResponse(exchange, "{\"message\":\"Invalid ticket format.\"}", 400, "application/json");
                }
            }
        });

        server.createContext("/api/view-tickets", (exchange) -> {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < supportTickets.size(); i++) {
                String[] t = supportTickets.get(i).split("\\|\\|");
                if (t.length >= 3) {
                    json.append(String.format("{\"id\":%d, \"user\":\"%s\", \"subject\":\"%s\", \"message\":\"%s\"}", 
                        i, t[0].replace("\"", "\\\""), t[1].replace("\"", "\\\""), t[2].replace("\"", "\\\"")));
                    if (i < supportTickets.size() - 1) json.append(",");
                }
            }
            json.append("]");
            sendResponse(exchange, json.toString(), 200, "application/json");
        });

        server.createContext("/api/resolve-ticket", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    int id = Integer.parseInt(readBody(exchange).trim());
                    if (id >= 0 && id < supportTickets.size()) {
                        supportTickets.remove(id);
                        auditLog.add(0, "Librarian resolved a support ticket.");
                    }
                    sendResponse(exchange, "{\"message\":\"Ticket resolved.\"}", 200, "application/json");
                } catch (Exception e) {
                    sendResponse(exchange, "{\"message\":\"Error resolving ticket.\"}", 400, "application/json");
                }
            }
        });

        // ==========================================
        // NEW: CURATED LISTS ENDPOINTS
        // ==========================================

        server.createContext("/api/get-curated-lists", (exchange) -> {
            StringBuilder json = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, List<String>> entry : curatedLists.entrySet()) {
                json.append("\"").append(entry.getKey()).append("\":[");
                List<String> keywords = entry.getValue();
                for (int j = 0; j < keywords.size(); j++) {
                    json.append("\"").append(keywords.get(j).replace("\"", "\\\"")).append("\"");
                    if (j < keywords.size() - 1) json.append(",");
                }
                json.append("]");
                if (i < curatedLists.size() - 1) json.append(",");
                i++;
            }
            json.append("}");
            sendResponse(exchange, json.toString(), 200, "application/json");
        });

        server.createContext("/api/add-curated-list", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String courseName = readBody(exchange).trim();
                if (!courseName.isEmpty() && !curatedLists.containsKey(courseName)) {
                    curatedLists.put(courseName, new ArrayList<>());
                    auditLog.add(0, "Librarian created new Curated List: " + courseName);
                    sendResponse(exchange, "{\"message\":\"List created successfully.\"}", 200, "application/json");
                } else {
                    sendResponse(exchange, "{\"message\":\"List already exists or invalid name.\"}", 400, "application/json");
                }
            }
        });

        server.createContext("/api/add-curated-keyword", (exchange) -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String[] parts = readBody(exchange).split("\\|\\|");
                if (parts.length == 2) {
                    String courseName = parts[0];
                    String keyword = parts[1];
                    if (curatedLists.containsKey(courseName)) {
                        curatedLists.get(courseName).add(keyword);
                        auditLog.add(0, "Librarian added resource '" + keyword + "' to list: " + courseName);
                        sendResponse(exchange, "{\"message\":\"Added to curated list.\"}", 200, "application/json");
                        return;
                    }
                }
                sendResponse(exchange, "{\"message\":\"Error adding to list.\"}", 400, "application/json");
            }
        });

        server.createContext("/", new StaticFileHandler());
        server.start();
        System.out.println("System Server active at http://localhost:8080");
    }

    // HELPERS

    private static void updateCode() {
        if (System.currentTimeMillis() > codeExpiryTime) {
            currentLibrarianCode = String.format("%04d", new Random().nextInt(10000));
            codeExpiryTime = System.currentTimeMillis() + 300000;
        } 
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ex.getRequestBody()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static String getQueryParam(HttpExchange ex, String key) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) return "";
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1 && pair[0].equals(key)) return pair[1];
        }
        return "";
    }

    private static void sendResponse(HttpExchange ex, String data, int status, String type) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, data.getBytes().length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data.getBytes()); }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("Frontend" + path);
            if (file.exists()) {
                String type = path.endsWith(".html") ? "text/html" : path.endsWith(".css") ? "text/css" : "text/javascript";
                t.getResponseHeaders().set("Content-Type", type);
                t.sendResponseHeaders(200, file.length());
                try (FileInputStream fs = new FileInputStream(file)) { fs.transferTo(t.getResponseBody()); }
                t.getResponseBody().close();
            } else { t.sendResponseHeaders(404, -1); }
        }
    }
}