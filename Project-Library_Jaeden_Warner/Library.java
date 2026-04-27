import java.util.Scanner;

public class Library{
    
    static Scanner scnr = new Scanner(System.in);
    static Database database = new Database();

    public static void main(String[] args){

        System.out.println("Welcome to Library Managemnet System");
        System.out.println("0. Exit");
        System.out.println("1. Login");
        System.out.println("2. New User");
        
        int n = scnr.nextInt();

        // Switch case for the login and new user
        switch(n){
            case 1:
                login();
                break;
            case 2:
                newUser();
                break;
        }

    }

    // login in method that checks if the user has been created
    private static void login(){
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
            Library.main(null);
        }

    }

    // new user allows a new user to be added to the database
    private static void newUser(){
        System.out.print("Enter name: ");
        String name = scnr.next();

        if(database.userExist(name)){
            System.out.println("User Exists!");
            newUser();
        }

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