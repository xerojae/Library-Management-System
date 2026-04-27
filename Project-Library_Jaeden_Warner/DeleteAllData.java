// DeleteAllData class implements ine Interface Class, IOOperation

import java.util.Scanner;

public class DeleteAllData implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We override the abtract method from the interface class, IOOperations
    @Override
    public void oper(Database database, User user){

        System.out.println("\nAre you sure you wanna delete all data?");
        System.out.println("1. Continue");
        System.out.println("2. Main Menu");
        
        // Giving the user the option to delete all the data
        int i = scnr.nextInt();
        if(i == 1){
            database.deleteAllData();
        }
        else{
            user.menu(database, user);
        }

    }

}