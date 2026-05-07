// PlaceOrder Class implements the interface class, IOOperation

import java.util.Scanner;

public class PlaceOrder implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We Override the abstract method from the abtract class, User
    @Override
    public void oper(Database database, User user){
        
        Order order = new Order();
        System.out.println("\nEnter book name: ");
        String bookname = scnr.next();

        // Check to see if the book exist
        int i = database.getBook(bookname);
        if(i <= -1){
            System.out.println("Book doesn't exist!");

        }
        // If the book doesnt exsit, tell the user
        // Else, the user will borrow the book and the qty will update
        else{
            Book book = database.getBook(i);
            order.setBook(database.getBook(i));
            order.setUser(user);

            System.out.println("Enter qty: ");
            int qty = scnr.nextInt();
            order.setQty(qty);

            order.setPrice(book.getPrice() * qty);
            int bookIndex = database.getBook(book.getName());
            book.setQTY(book.getQTY() - qty);
            database.addOrder(order, book, bookIndex);

            System.out.println("Order Placed!\n");

        }

        // Return to the menu
        user.menu(database, user);

    }
    
}