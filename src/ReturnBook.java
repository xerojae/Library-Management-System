// ReturnBook Class implements the interface class, IOOperation

import java.time.LocalDate;
import java.util.Scanner;

public class ReturnBook implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We Override the abstract method from the interface class, IOOperation
    @Override
    public void oper(Database database, User user){

        System.out.println("Enter book name: ");
        String bookname = scnr.next();

        if(!database.getBrws().isEmpty()){
            for(Borrowing b : database.getBrws()){
                if(b.getBook().getName().matches(bookname) && b.getUser().getName().matches(user.getName())){
                    Book book = b.getBook();
                    int i = database.getAllBooks().indexOf(book);
                    if(b.getDaysLeft() < 0){
                        System.out.println("You are late!\n" + "You have to pay $" + Math.abs(b.getDaysLeft()) * 50 + " as fine.");
                    }
                    book.setBrwCopies(book.getBrwCopies() + 1);
                    database.returnBook(b, book, i);
                    System.out.println("Book returned\nThank you!\n");
                    break;
                }
                else{
                    System.out.println("You didn't borrow this book!");
                }
            }
        }
        else{
            System.out.println("You didn't borrow this book!\n");
        }
        user.menu(database, user);

    }
}