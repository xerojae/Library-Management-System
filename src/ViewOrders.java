import java.util.Scanner;

public class ViewOrders implements IOOperation{

    static Scanner scnr = new Scanner(System.in);

    // We Override the abstract method from the interface class, IOOperation
    @Override
    public void oper(Database database, User user){

        System.out.println("\nEnter book name: ");
        String bookname = scnr.next();

        // View the orders made from the normal users
        int i = database.getBook(bookname);
        if(i > -1){
            System.out.println("Book\t\tUser\t\tQty\t\tPrice");
            for(Order order : database.getAllOrders()){
                if(order.getBook().getName().matches(bookname)){
                    System.out.println(order.getBook().getName() + "\t\t" + 
                                        order.getUser().getName() + "\t\t" + 
                                        order.getQty() + "\t\t" + 
                                        order.getPrice());
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