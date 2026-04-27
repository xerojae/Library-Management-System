// NormalUser class extends the abstarct class, User

import java.util.Scanner;

public class NormalUser extends User{

    static Scanner scnr = new Scanner(System.in);

    public NormalUser(String name){
        super(name);
        this.operations = new IOOperation[]{
            new ViewBooks(),
            new Search(),
            new PlaceOrder(),
            new BorrowBook(),
            new CalculateFine(),
            new ReturnBook(),
            new Exit()
        };
    }

    // This method will give the user normal options when logged in as an admin user
    public NormalUser(String name, String email, String phoneNum){
        super(name, email, phoneNum);
        this.operations = new IOOperation[]{
            new ViewBooks(),
            new Search(),
            new PlaceOrder(),
            new BorrowBook(),
            new CalculateFine(),
            new ReturnBook(),
            new Exit()
        };
    }

    // We Override the abstract method from the abtract class, User
    @Override
    public void menu(Database database, User user){
        System.out.println("1. View Books");
        System.out.println("2. Search");
        System.out.println("3. Place Order");
        System.out.println("4. Borrow Book");
        System.out.println("5. Calculate Fine");
        System.out.println("6. Return Book");
        System.out.println("7. Exit");

        int n = scnr.nextInt();
        this.operations[n-1].oper(database, user);
        scnr.close();

    }

    // This method will show up in the "users.txt" file when called
    public String toString(){
        return name + "<N/>" + email + "<N/>" + phoneNum + "<N/>" + "Normal";
    }


}