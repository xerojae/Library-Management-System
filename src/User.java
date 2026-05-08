// User abstract class

public abstract class User{

    protected String name;
    protected String email;
    protected String phoneNum;
    protected IOOperation[] operations;

    public User(){

    }

    public User(String name){
        this.name = name;
    }

    // All the setter and getters
    public User(String name, String email, String phoneNum){
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getPhoneNum(){
        return phoneNum;
    }

    // abstract method toString
    abstract public String toString();

    // abtract method menu
    abstract public void menu(Database database, User user);

}