// Search class implements the interface class, IOOperation

import java.util.Scanner;

public class Search implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We Override the abstract method from the interface class, IOOperation
    @Override
    public void oper(Database database, User user){

        System.out.println("Enter book name: ");
        String name = scnr.next();

        int i = database.getBook(name);
        if(i > -1){
            System.out.println("\n"+ database.getBook(i).toString() + "\n");
        }
        else{
            System.out.println("Book doesn't exist!\n");
        }

        user.menu(database, user);

    }

}