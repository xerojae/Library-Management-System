import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Database{

    // Array list of books, booknames, users, usernames, orders, and borrowings
    private ArrayList<User> users = new ArrayList<User>();
    private ArrayList<String> usernames = new ArrayList<String>();
    private ArrayList<Book> books = new ArrayList<Book>();
    private ArrayList<String> booknames = new ArrayList<String>();
    private ArrayList<Order> orders = new ArrayList<Order>();
    private ArrayList<Borrowing> borrowings = new ArrayList<Borrowing>();

    // text files that contain users, books, orders, and borrowings
    private File usersfile = new File("users.txt");
    private File booksfile = new File("books.txt");
    private File ordersfile = new File("orders.txt");
    private File borrowingsfile = new File("borrowings.txt");

    // get the files of orders, users, books, and borrowings
    public Database(){
        if(!usersfile.exists()){
            try{
                usersfile.createNewFile();
            }
            catch(Exception e){}
        }
        if(!booksfile.exists()){
            try{
                booksfile.createNewFile();
            }
            catch(Exception e){}
        }
        if(!ordersfile.exists()){
            try{
                ordersfile.createNewFile();
            }
            catch(Exception e){}
        }
        if(!borrowingsfile.exists()){
            try{
                borrowingsfile.createNewFile();
            }
            catch(Exception e){}
        }
        getUsers();
        getBooks();
        getOrders();
        getBorrowings();
    }

    public void AddUser(User s){
        users.add(s);
        usernames.add(s.getName());
        saveUsers();
    }

    public User getUser(int n){
        return users.get(n);
    }

    public int login(String phoneNum, String email){
        int n = -1;
        for(User s : users){
            if(s.getPhoneNum().matches(phoneNum) && s.getEmail().matches(email)){
                n = users.indexOf(s);
                break;
            }
        }
        return n;
    }

    private void getUsers(){
        String text1 = "";
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(usersfile));
            String s1;
            while((s1 = br1.readLine()) != null){
                text1 = text1 + s1;
            }
            br1.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }

        if(!text1.matches("") || !text1.isEmpty()){
            String[] a1 = text1.split("<NewUser/>>");
            for(String s : a1){
                String[] a2 = s.split("<N/>");
                if(a2[3].matches("Admin")){
                    User user = new Admin(a2[0], a2[1], a2[2]);
                    users.add(user);
                    usernames.add(user.getName());
                }
                else{
                    User user = new NormalUser(a2[0], a2[1], a2[2]);
                    users.add(user);
                    usernames.add(user.getName());
                }
            }
        }
    }

    private void saveUsers(){
        String text1 = "";
        for(User user : users){
            text1 = text1 + user.toString() + "<NewUser/>\n>";
        }
        try{
            PrintWriter pw = new PrintWriter(usersfile);
            pw.print(text1);
            pw.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }
    }

    public boolean userExist(String name){
        boolean f = false;
        for(User user : users){
            if(user.getName().toLowerCase().matches(name.toLowerCase())){
                f = true;
                break;
            }
        }
        return f;
    }

    private User getUserByName(String name){
        User u = new NormalUser("");
        for(User user : users){
            if(user .getName().matches(name)){
                u = user;
                break;
            }
        }
        return u;
    }

    public void AddBook(Book book){
        books.add(book);
        booknames.add(book.getName());
        saveBooks();
    }

    private void saveBooks(){
        String text1 = "";
        for(Book book : books){
            text1 = text1 + book.toString2() + "<NewBook/>\n>";
        }
        try{
            PrintWriter pw = new PrintWriter(booksfile);
            pw.print(text1);
            pw.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }
    }

    // adds a book to the "books.txt" file
    private void getBooks(){
        String text1 = "";
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(booksfile));
            String s1;
            while((s1 = br1.readLine()) != null){
                text1 = text1 + s1;
            }
            br1.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }

        if(!text1.matches("") || !text1.isEmpty()){
            String[] a1 = text1.split("<NewBook/>>");
            for(String s : a1){
                Book book = parseBook(s);
                books.add(book);
                booknames.add(book.getName());
            }
        }
    }

    public Book parseBook(String s){
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

    public ArrayList<Book> getAllBooks(){
        return books;
    }

    public int getBook(String bookname){
        int i = -1;
        for(Book book : books){
            if(book.getName().matches(bookname)){
                i = books.indexOf(book);
            }
        }
        return i;
    }

    public Book getBook(int i){
        return books.get(i);
    }

    public void deleteBook(int i){
        books.remove(i);
        booknames.remove(i);
        saveBooks();
    }

    // This method will delete all date from all files
    public void deleteAllData(){
        if(usersfile.exists()){
            try{
                usersfile.delete();
            }
            catch(Exception e){}
        }
        if(booksfile.exists()){
            try{
                booksfile.delete();
            }
            catch(Exception e){}
        }
        if(ordersfile.exists()){
            try{
                ordersfile.delete();
            }
            catch(Exception e){}
        }
        if(borrowingsfile.exists()){
            try{
                borrowingsfile.delete();
            }
            catch(Exception e){}
        }
    }

    public void addOrder(Order order, Book book, int bookIndex){
        orders.add(order);
        books.set(bookIndex, book);
        saveOrders();
        saveBooks();
    }

    private void saveOrders(){
        String text1 = "";
        for(Order order : orders){
            text1 = text1 + order.toString2() + "<NewOrder/>\n>";
        }
        try{
            PrintWriter pw = new PrintWriter(ordersfile);
            pw.print(text1);
            pw.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }
    }

    // adds a order to the "orders.txt" file
    private void getOrders(){
        String text1 = "";
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(ordersfile));
            String s1;
            while((s1 = br1.readLine()) != null){
                text1 = text1 + s1;
            }
            br1.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }

        if(!text1.matches("") || !text1.isEmpty()){
            String[] a1 = text1.split("<NewOrder/>>");
            for(String s : a1){
                Order order = parseOrder(s);
                orders.add(order);
            }
        }
    }

    private Order parseOrder(String s){
        String[] a = s.split("<N/>");
        Order order = new Order(books.get(getBook(a[0])), getUserByName(a[1]), 
                                Double.parseDouble(a[2]), Integer.parseInt(a[3]));
        return order;
    }

    public ArrayList<Order> getAllOrders(){
        return orders;
    }

    public ArrayList<Borrowing> getAllBorrowings(){
        return borrowings;
    }


    private void saveBorrowings(){
        String text1 = "";
        for(Borrowing borrowing : borrowings){
            text1 = text1 + borrowing.toString2() + "<NewBorrowing/>\n>";
        }
        try{
            PrintWriter pw = new PrintWriter(borrowingsfile);
            pw.print(text1);
            pw.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }
    }

    // adds a borrowed book to the "borrowings.txt" file
    private void getBorrowings(){
        String text1 = "";
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(borrowingsfile));
            String s1;
            while((s1 = br1.readLine()) != null){
                text1 = text1 + s1;
            }
            br1.close();
        }
        catch(Exception e){
            System.err.println(e.toString());
        }

        if(!text1.matches("") || !text1.isEmpty()){
            String[] a1 = text1.split("<NewBorrowing/>>");
            for(String s : a1){
                Borrowing borrowing = parseBorrowing(s);
                borrowings.add(borrowing);
            }
        }
    }

    private Borrowing parseBorrowing(String s){
        String[] a = s.split("<N/>");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(a[0], formatter);
        LocalDate finish = LocalDate.parse(a[1], formatter);
        Book book = getBook(getBook(a[3]));
        User user = getUserByName(a[4]);
        Borrowing brw = new Borrowing(start, finish, book, user);
        return brw;
    }

    public void borrowBook(Borrowing brw, Book book, int bookIndex){
        borrowings.add(brw);
        books.set(bookIndex, book);
        saveBorrowings();
        saveBooks();
    }

    public ArrayList<Borrowing> getBrws(){
        return borrowings;
    }

    public void returnBook(Borrowing b, Book book, int bookIndex){
        borrowings.remove(b);
        books.set(bookIndex, book);
        saveBorrowings();
        saveBooks();
    }

}