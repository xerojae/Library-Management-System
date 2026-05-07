// Admin class extends the abtract class, User

import java.util.Scanner;

public class Admin extends User{

    static Scanner scnr = new Scanner(System.in);

    public Admin(String name){
        super(name);
        this.operations = new IOOperation[]{
            new ViewBooks(),
            new AddBook(),
            new DeleteBook(),
            new Search(),
            new DeleteAllData(),
            new ViewOrders(),
            new Exit()
        };
    }

    // This method will give the user admin options when logged in as an admin user
    public Admin(String name, String email, String phoneNum){
        super(name, email, phoneNum);
        this.operations = new IOOperation[]{
            new ViewBooks(),
            new AddBook(),
            new DeleteBook(),
            new Search(),
            new DeleteAllData(),
            new ViewBorrowings(),
            new ViewOrders(),
            new Exit()
        };
    }

    // We Override the abstract method from the abtract class, User
    @Override
    public void menu(Database database, User user){
        System.out.println("1. View Books");
        System.out.println("2. Add Book");
        System.out.println("3. Delete Book");
        System.out.println("4. Search");
        System.out.println("5. Delete All Data");
        System.out.println("6. View Borrowings");
        System.out.println("7. View Orders");
        System.out.println("8. Exit");

        int n = scnr.nextInt();
        this.operations[n-1].oper(database, user);
        scnr.close();

    }

    // This method will show up in the "users.txt" file when called
    public String toString(){
        return name + "<N/>" + email + "<N/>" + phoneNum + "<N/>" + "Admin";
    }

}