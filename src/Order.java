// Order Class

public class Order{

    private Book book;
    private User user;
    private double price;
    private int qty;

    public Order(){
        
    }

    public Order(Book book, User user, double price, int qty){
        this.book = book;
        this.user = user;
        this.price = price;
        this.qty = qty;
    }

    // All the setter and getters
    public void setBook(Book book){
        this.book = book;
    }

    public void setUser(User user){
        this.user = user;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public void setQty(int qty){
        this.qty = qty;
    }

    public Book getBook(){
        return book;
    }

    public User getUser(){
        return user;
    }

    public double getPrice(){
        return price;
    }

    public int getQty(){
        return qty;
    }

    public String toString(){
        return "Book name: " + book.getName() + "\n" + 
                "Username: " + user.getName() + "\n" + 
                "Price: " + String.valueOf(price) + "\n" +
                "Qty: " + String.valueOf(qty);
    }

    // This method will show up in the "orders.txt" file when called
    public String toString2(){
        return book.getName() + "<N/>" + user.getName() + "<N/>" + String.valueOf(price) + "<N/>" + String.valueOf(qty);
    }

}