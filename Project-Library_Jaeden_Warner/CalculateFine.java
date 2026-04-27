// CalculateFine Class implements the Interface Class, IOOperation

import java.util.Scanner;

public class CalculateFine implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We override the abtract method from the interface class, IOOperations
    @Override
    public void oper(Database database, User user){

        System.out.println("Enter book name: ");
        String bookname = scnr.next();

        for(Borrowing b : database.getBrws()){
            if(b.getBook().getName().matches(bookname) && b.getUser().getName().matches(user.getName())){
                if(b.getDaysLeft() < 0){
                    System.out.println("You are late!\n" + "You have to pay $" + Math.abs(b.getDaysLeft()) * 50 + " as fine.");
                }
                else{
                    System.out.println("You don't have to pay fine.\n");
                }
            }
        }

        user.menu(database, user);

    }

}