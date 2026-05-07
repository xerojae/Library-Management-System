// BorrowBook class implents the Interface Class, IOOperations 

import java.util.Scanner;

public class BorrowBook implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We override the abtract method from the Interface Class, IOOperation
    @Override
    public void oper(Database database, User user){
        
        System.out.println("Enter book name: ");
        String bookname = scnr.next();

        int i = database.getBook(bookname);
        if(i > -1){
            Book book = database.getBook(i);

            boolean n = true;
            for(Borrowing b : database.getBrws()){
                if(b.getBook().getName().matches(bookname) && b.getUser().getName().matches(user.getName())){
                    n = false;
                    System.out.println("You have borrowed this book before!\n");
                }
            }

            if(n){
                if(book.getBrwCopies() > 1){
                    Borrowing borrowing = new Borrowing(book, user);
                    book.setBrwCopies(book.getBrwCopies() - 1);
                    database.borrowBook(borrowing, book, i);
                    System.out.println("You must return the book before 14 days from now\n" + 
                                        "Expiry date: " + borrowing.getFinish() + "\nEnjoy!\n");
                }
                else{
                    System.out.println("This book isn't available for borrowing\n");
                }
            }

        }
        else{
            System.out.println("Book doesn't exist!\n");
        }
        user.menu(database, user);

    }
}