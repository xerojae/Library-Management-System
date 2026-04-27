import java.util.ArrayList;

public class ViewBooks implements IOOperation{

    // We Override the abstract method from the interface class, IOOperation
    @Override
    public void oper(Database database, User user){

        // Make an Array list of to show them when the user wants to
        ArrayList<Book> books = database.getAllBooks();
        System.out.println("Name\t\tAuthor\t\tPublisher\tCLA\tQty\tPrice\tBRW Copies");

        for(Book b : books){
            System.out.println(b.getName() + "\t\t" + b.getAuthor() + "\t\t" + b.getPublisher() + "\t\t" + b.getAddress() + 
                                "\t" + b.getQTY() + "\t" + b.getPrice() + "\t" + b.getBrwCopies());
        }

        System.out.println();
        user.menu(database, user);

    }

}