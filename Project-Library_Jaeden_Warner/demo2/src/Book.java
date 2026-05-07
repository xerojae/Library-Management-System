// Book Class

import java.util.ArrayList;

public class Book{

    private String name;        //title
    private String author;      //author
    private String publisher;   //publisher
    private String address;     //collection location
    private String status;      //borrowing status
    private int qty;            //copies for sale
    private double price;       //price
    private int brwcopies;      //copies for borrowing

    public Book(){
        
    };

    public Book(String name, String author, String publisher, String address, int qty, double price, int brwcopies){
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.address = address;
        this.qty = qty;
        this.price = price;
        this.brwcopies = brwcopies;
    }

    public String toString(){
        String text = "Book Name: " + name + "\nBook Author: " + author + 
                    "\nBook Publisher: " + publisher + "\nBook Collection Address: " + address + 
                    "\nQty: " + String.valueOf(qty) + "\nPrice: " + String.valueOf(price) +
                    "\nBorrowing Copies: " + String.valueOf(brwcopies);
        return text;
    }

    // All the setter and getters
    public void setName(String name){
        this.name = name;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public void setPublisher(String publisher){
        this.publisher = publisher;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public void setQTY(int qty){
        this.qty = qty;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public void setBrwCopies(int brwcopies){
        this.brwcopies = brwcopies;
    }

    public String getName(){
        return name;
    }

    public String getAuthor(){
        return author;
    }

    public String getPublisher(){
        return publisher;
    }

    public String getAddress(){
        return address;
    }

    public String getStatus(){
        return status;
    }

    public int getQTY(){
        return qty;
    }

    public double getPrice(){
        return price;
    }

    public int getBrwCopies(){
        return brwcopies;
    }

    // This method will show up in the "books.txt" file when called
    public String toString2(){
        String text = name + "<N/>" + author + "<N/>" + publisher + "<N/>" + address + 
                    "<N/>" + String.valueOf(qty) + "<N/>" + String.valueOf(price) +
                    "<N/>" + String.valueOf(brwcopies);
        return text;
    }

}