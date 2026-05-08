// AddBook class that implemnts the Interface Class, IOOperation

import java.util.Scanner;

public class AddBook implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // Here we override from the Interface Class, IOOperation
    @Override
    public void oper(Database database, User user){

        Book book = new Book();

        System.out.println("Enter book name: ");
        String name = scnr.next();

        // Check to see if the book ws already added
        if(database.getBook(name) > -1){
            System.out.println("There is already a book with this name!\n");
            user.menu(database, user);
            return;
        }

        // If the book hasent been created, then the user can Add it
        else{
            book.setName(name);

            System.out.println("Enter book author: ");
            book.setAuthor(scnr.next());

            System.out.println("Enter book publisher: ");
            book.setPublisher(scnr.next());

            System.out.println("Enter book collection address: ");
            book.setAddress(scnr.next());

            System.out.println("Enter qty: ");
            book.setQTY(scnr.nextInt());

            System.out.println("Enter price: ");
            book.setPrice(scnr.nextDouble());

            System.out.println("Enter borrowing copies: ");
            book.setBrwCopies(scnr.nextInt());

            database.AddBook(book);
            System.out.println("Book Added!\n");

            user.menu(database, user);
        }
    }
}