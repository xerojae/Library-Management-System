import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Database {

    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<String> usernames = new ArrayList<String>();
    private ArrayList<Book> books = new ArrayList<Book>();
    private ArrayList<String> booknames = new ArrayList<String>();
    private ArrayList<Order> orders = new ArrayList<Order>();
    private ArrayList<Borrowing> borrowings = new ArrayList<Borrowing>();

    private File usersfile = new File("users.txt");
    private File booksfile = new File("books.txt");
    private File ordersfile = new File("orders.txt");
    private File borrowingsfile = new File("borrowings.txt");

    public Database(){
        if(!usersfile.exists()){ try{ usersfile.createNewFile(); } catch(Exception e){} }
        if(!booksfile.exists()){ try{ booksfile.createNewFile(); } catch(Exception e){} }
        if(!ordersfile.exists()){ try{ ordersfile.createNewFile(); } catch(Exception e){} }
        if(!borrowingsfile.exists()){ try{ borrowingsfile.createNewFile(); } catch(Exception e){} }
        getUsers();
        getBooks();
        getOrders();
        getBorrowings();
    }

    public void addBook(Book book) {
        AddBook(book);
    }

    public void deleteBook(String name) {
        int index = getBook(name);
        if (index != -1) {
            deleteBook(index);
        }
    }

    public void deleteAllData(){
        users.clear();
        usernames.clear();
        books.clear();
        booknames.clear();
        orders.clear();
        borrowings.clear();

        if(usersfile.exists()){ try{ usersfile.delete(); } catch(Exception e){} }
        if(booksfile.exists()){ try{ booksfile.delete(); } catch(Exception e){} }
        if(ordersfile.exists()){ try{ ordersfile.delete(); } catch(Exception e){} }
        if(borrowingsfile.exists()){ try{ borrowingsfile.delete(); } catch(Exception e){} }
    }

    public ArrayList<Borrowing> getBrws(){
        return borrowings;
    }

    public void AddBook(Book book){
        books.add(book);
        booknames.add(book.getName());
        saveBooks();
    }

    public void deleteBook(int i){
        books.remove(i);
        booknames.remove(i);
        saveBooks();
    }

    public void AddUser(User s){
        users.add(s);
        usernames.add(s.getName());
        saveUsers();
    }

    public User getUser(int n){ 
        if (n < 0 || n >= users.size()) return null; 
        return users.get(n); 
    }

    public int login(String phoneNum, String email){
        int n = -1;
        for(int i = 0; i < users.size(); i++){
            User s = users.get(i);
            if(s.getPhoneNum().equals(phoneNum) && s.getEmail().equalsIgnoreCase(email)){
                n = i;
                break;
            }
            if(s.getEmail().equals(phoneNum) && s.getPhoneNum().equalsIgnoreCase(email)){
                n = i;
                break;
            }
        }
        return n;
    }

    private void getUsers(){
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(usersfile));
            String s1;
            while((s1 = br1.readLine()) != null) {
                s1 = s1.trim();
                if(s1.startsWith(">")) s1 = s1.substring(1); // Safely remove ghost char
                s1 = s1.replace("<NewUser/>", "").replace("<NewUser/", ""); 
                s1 = s1.replace("<N/>", "<N/").replace("<N/", "<N/>"); // AUTO-HEAL broken tags

                if(s1.isEmpty()) continue;
                
                String[] a2 = s1.split("<N/>");
                if(a2.length < 4) continue;
                
                User user = a2[3].contains("Admin") ? new Admin(a2[0], a2[1], a2[2]) : new NormalUser(a2[0], a2[1], a2[2]);
                users.add(user);
                usernames.add(user.getName());
            }
            br1.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    private void saveUsers(){
        StringBuilder text1 = new StringBuilder();
        for(User user : users){ text1.append(user.toString()).append("<NewUser/>\n"); }
        try{
            PrintWriter pw = new PrintWriter(usersfile);
            pw.print(text1.toString());
            pw.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    public boolean userExist(String name){
        for(User user : users){
            if(user.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private User getUserByName(String name){
        for(User user : users){
            if(user.getName().equalsIgnoreCase(name) || user.getEmail().equalsIgnoreCase(name)) return user;
        }
        return users.isEmpty() ? null : users.get(0);
    }

    private void saveBooks(){
        StringBuilder text1 = new StringBuilder();
        for(Book book : books){ text1.append(book.toString2()).append("<NewBook/>\n"); }
        try{
            PrintWriter pw = new PrintWriter(booksfile);
            pw.print(text1.toString());
            pw.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    private void getBooks(){
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(booksfile));
            String s1;
            while((s1 = br1.readLine()) != null) {
                s1 = s1.trim();
                if(s1.startsWith(">")) s1 = s1.substring(1); 
                s1 = s1.replace("<NewBook/>", "").replace("<NewBook/", "");
                s1 = s1.replace("<N/>", "<N/").replace("<N/", "<N/>"); // AUTO-HEAL

                if(s1.isEmpty()) continue;
                
                Book book = parseBook(s1);
                books.add(book);
                booknames.add(book.getName());
            }
            br1.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    public Book parseBook(String s){
        s = s.trim();
        s = s.replace("<N/>", "<N/").replace("<N/", "<N/>"); // AUTO-HEAL
        String[] a = s.split("<N/>");
        Book book = new Book();
        book.setName(a[0]);
        book.setAuthor(a[1]);
        book.setPublisher(a[2]);
        book.setAddress(a[3]);
        book.setQTY(Integer.parseInt(a[4]));
        book.setPrice(Double.parseDouble(a[5]));
        book.setBrwCopies(Integer.parseInt(a[6]));
        return book;
    }

    public ArrayList<Book> getAllBooks(){ return books; }

    public int getBook(String bookname){
        for(int i = 0; i < books.size(); i++){
            if(books.get(i).getName().equalsIgnoreCase(bookname)) return i;
        }
        return -1;
    }

    public Book getBook(int i){ 
        if (i < 0 || i >= books.size()) return null;
        return books.get(i); 
    }

    public void addOrder(Order order, Book book, int bookIndex){
        orders.add(order);
        books.set(bookIndex, book);
        saveOrders();
        saveBooks();
    }

    private void saveOrders(){
        StringBuilder text1 = new StringBuilder();
        for(Order order : orders){ text1.append(order.toString2()).append("<NewOrder/>\n"); }
        try{
            PrintWriter pw = new PrintWriter(ordersfile);
            pw.print(text1.toString());
            pw.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    private void getOrders(){
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(ordersfile));
            String s1;
            while((s1 = br1.readLine()) != null) {
                s1 = s1.trim();
                if(s1.startsWith(">")) s1 = s1.substring(1);
                s1 = s1.replace("<NewOrder/>", "").replace("<NewOrder/", "");
                s1 = s1.replace("<N/>", "<N/").replace("<N/", "<N/>"); // AUTO-HEAL

                if(s1.isEmpty()) continue;
                orders.add(parseOrder(s1));
            }
            br1.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    private Order parseOrder(String s){
        s = s.trim();
        s = s.replace("<N/>", "<N/").replace("<N/", "<N/>"); // AUTO-HEAL
        String[] a = s.split("<N/>");
        return new Order(books.get(getBook(a[0])), getUserByName(a[1]), 
                         Double.parseDouble(a[2]), Integer.parseInt(a[3]));
    }

    public ArrayList<Order> getAllOrders(){ return orders; }
    public ArrayList<Borrowing> getAllBorrowings(){ return borrowings; }

    private void saveBorrowings(){
        StringBuilder text1 = new StringBuilder();
        for(Borrowing borrowing : borrowings){ text1.append(borrowing.toString2()).append("<NewBorrowing/>\n"); }
        try{
            PrintWriter pw = new PrintWriter(borrowingsfile);
            pw.print(text1.toString());
            pw.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    private void getBorrowings(){
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(borrowingsfile));
            String s1;
            while((s1 = br1.readLine()) != null) {
                s1 = s1.trim();
                if(s1.startsWith(">")) s1 = s1.substring(1);
                s1 = s1.replace("<NewBorrowing/>", "").replace("<NewBorrowing/", "");
                s1 = s1.replace("<N/>", "<N/").replace("<N/", "<N/>"); // AUTO-HEAL

                if(s1.isEmpty()) continue;
                borrowings.add(parseBorrowing(s1));
            }
            br1.close();
        } catch(Exception e){ System.err.println(e.toString()); }
    }

    private Borrowing parseBorrowing(String s){
        s = s.trim();
        s = s.replace("<N/>", "<N/").replace("<N/", "<N/>"); // AUTO-HEAL
        String[] a = s.split("<N/>");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(a[0], formatter);
        LocalDate finish = LocalDate.parse(a[1], formatter);
        
        int bookIdx = getBook(a[3]);
        Book book = (bookIdx != -1) ? getBook(bookIdx) : new Book();
        User user = getUserByName(a[4]);
        
        return new Borrowing(start, finish, book, user);
    }

    public void borrowBook(Borrowing brw, Book book, int bookIndex){
        borrowings.add(brw);
        books.set(bookIndex, book);
        saveBorrowings();
        saveBooks();
    }

    public void returnBook(Borrowing b, Book book, int bookIndex){
        borrowings.remove(b);
        books.set(bookIndex, book);
        saveBorrowings();
        saveBooks();
    }
}