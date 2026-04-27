// Exit Class implements the Interface Class, IOOperations

import java.util.Scanner;

public class Exit implements IOOperation{

    static Scanner scnr = new Scanner(System.in);
    Database database;
    User user;

    // We override the abtract method from the interface class, IOOperations
    @Override
    public void oper(Database database, User user){
        // using "this" to keep the same database
        this.database = database;
        this.user = user;

        System.out.println("\nAre you sure you wanna quit?");
        System.out.println("1. Yes");
        System.out.println("2. Main Menu");
        
        // Asking if the user wants to quit and sending them back to the main login page
        System.out.println();
        int i = scnr.nextInt();
        if(i == 1){
            System.out.println("Welcome to Library MAnagemnet System");
            System.out.println("0. Exit");
            System.out.println("1. Login");
            System.out.println("2. New User");
        
            int n = scnr.nextInt();

            switch(n){
                case 1:
                    login();
                case 2:
                    newUser();
            }
        }
        else{
            user.menu(database, user);
        }

    }

    private void login(){
        System.out.print("Enter phone number: ");
        String phoneNum = scnr.next();

        System.out.print("Enter email: ");
        String email = scnr.next();

        int n = database.login(phoneNum, email);
        if(n != -1){
            User user = database.getUser(n);
            System.out.println("Welcome " + user.getName() + "!");
            user.menu(database, user);
        }
        else{
            System.out.println("User doesn't exist!");
        }

    }

    private void newUser(){
        System.out.print("Enter name: ");
        String name = scnr.next();

        System.out.print("Enter phone number: ");
        String phoneNum = scnr.next();

        System.out.print("Enter email: ");
        String email = scnr.next();

        System.out.println("1. Admin");
        System.out.println("2. Normal User");
        int n2 = scnr.nextInt();
        User user;
        if(n2 == 1){
            user = new Admin(name, email, phoneNum);
        }
        else{
            user = new NormalUser(name, email, phoneNum);
        }
        database.AddUser(user);
        user.menu(database, user);

    }

}