import java.util.Scanner;

public class ViewBorrowings implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We Override the abstract method from the interface class, IOOperation
    @Override
    public void oper(Database database, User user){

        System.out.println("\nEnter book name: ");
        String bookname = scnr.next();

        // View the orders made from the normal users
        int i = database.getBook(bookname);
        if(i > -1){
            System.out.println("Book\t\tUser\t\tStart\t\tFinish");
            for(Borrowing borrowing : database.getAllBorrowings()){
                if(borrowing.getBook().getName().matches(bookname)){
                    System.out.println(borrowing.getBook().getName() + "\t\t" + 
                                        borrowing.getUser().getName() + "\t\t" + 
                                        borrowing.getStart() + "\t" + 
                                        borrowing.getFinish());
                }
            }
            System.out.println();
        }

        // If the book doesnt exist, tell the user
        else{
            System.out.println("Book doesn't exist!\n");
        }
        user.menu(database, user);

    }

}