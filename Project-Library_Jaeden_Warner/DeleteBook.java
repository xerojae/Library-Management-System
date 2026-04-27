// DeleteBook class implements the Interface CLass, IOOperation

import java.util.Scanner;

public class DeleteBook implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We override the abtract method from the interface class, IOOperations
    @Override
    public void oper(Database database, User user){

        System.out.println("Enter book name: ");
        String bookname = scnr.next();

        // If the book exist, it will be deleted
        int i = database.getBook(bookname);
        if(i > -1){
            database.deleteBook(i);
            System.out.println("Book deleted!\n");
        }
        // Tell the user if the book doesnt exist
        else{
            System.out.println("Book doesn't exist!\n");
        }

        // Return to the menu
        user.menu(database, user);

    }

}