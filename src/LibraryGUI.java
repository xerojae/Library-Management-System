import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LibraryGUI {

    private JFrame frame;
    private JPanel contentPanel;
    private Database database = new Database();
    private User currentUser;

    public LibraryGUI() {
        frame = new JFrame("Library System");
        frame.setSize(1000, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        showLoginScreen();
        frame.setVisible(true);
    }

    // ================= LOGIN =================
    private void showLoginScreen() {
        frame.getContentPane().removeAll();

        JPanel panel = new JPanel(new GridBagLayout());
        JPanel card = new JPanel(new GridLayout(6, 1, 10, 10));
        card.setPreferredSize(new Dimension(300, 280));

        JTextField phone = new JTextField();
        JTextField email = new JTextField();

        JButton login = new JButton("Login");
        JButton register = new JButton("Register");

        card.add(new JLabel("Phone"));
        card.add(phone);
        card.add(new JLabel("Email"));
        card.add(email);
        card.add(login);
        card.add(register);

        panel.add(card);
        frame.add(panel);

        login.addActionListener(e -> {
            int index = database.login(phone.getText(), email.getText());
            if (index != -1) {
                currentUser = database.getUser(index);
                showDashboard();
            } else {
                JOptionPane.showMessageDialog(frame, "User not found");
            }
        });

        register.addActionListener(e -> registerUser());

        frame.revalidate();
        frame.repaint();
    }

    // ================= REGISTER =================
    private void registerUser() {
        frame.getContentPane().removeAll();

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Normal User"});

        JButton submit = new JButton("Create Account");
        JButton back = new JButton("Back");

        panel.add(new JLabel("Name:")); panel.add(name);
        panel.add(new JLabel("Phone:")); panel.add(phone);
        panel.add(new JLabel("Email:")); panel.add(email);
        panel.add(new JLabel("Role:")); panel.add(roleBox);
        panel.add(submit); panel.add(back);

        frame.add(panel);

        submit.addActionListener(e -> {
            if (database.userExist(name.getText())) {
                JOptionPane.showMessageDialog(frame, "User exists!");
                return;
            }

            User user;

            if (roleBox.getSelectedItem().equals("Admin")) {
                while (true) {
                    String pass = JOptionPane.showInputDialog("Enter admin password:");
                    if (pass == null) return;
                    if (pass.equals("admin1")) {
                        user = new Admin(name.getText(), email.getText(), phone.getText());
                        break;
                    }
                    JOptionPane.showMessageDialog(frame, "Wrong password");
                }
            } else {
                user = new NormalUser(name.getText(), email.getText(), phone.getText());
            }

            database.AddUser(user);
            JOptionPane.showMessageDialog(frame, "Account created!");
            showLoginScreen();
        });

        back.addActionListener(e -> showLoginScreen());

        frame.revalidate();
        frame.repaint();
    }

    // ================= DASHBOARD =================
    private void showDashboard() {
        frame.getContentPane().removeAll();

        JPanel sidebar = new JPanel(new GridLayout(12, 1));
        sidebar.setPreferredSize(new Dimension(200, 0));

        contentPanel = new JPanel(new BorderLayout());

        JButton viewBooks = new JButton("View Books");
        JButton logout = new JButton("Logout");

        sidebar.add(viewBooks);

        if (currentUser instanceof Admin) {

            JButton addBook = new JButton("Add Book");
            JButton deleteBook = new JButton("Delete Book");
            JButton search = new JButton("Search");
            JButton deleteAll = new JButton("Delete All Data");
            JButton borrowings = new JButton("View Borrowings");
            JButton orders = new JButton("View Orders");

            sidebar.add(addBook);
            sidebar.add(deleteBook);
            sidebar.add(search);
            sidebar.add(deleteAll);
            sidebar.add(borrowings);
            sidebar.add(orders);

            addBook.addActionListener(e -> addBookForm());
            deleteBook.addActionListener(e -> deleteBookForm());
            search.addActionListener(e -> searchForm());
            deleteAll.addActionListener(e -> deleteAllData());
            borrowings.addActionListener(e -> viewBorrowings());
            orders.addActionListener(e -> viewOrders());

        } else {

            JButton search = new JButton("Search");
            JButton order = new JButton("Place Order");
            JButton borrow = new JButton("Borrow Book");
            JButton fine = new JButton("Calculate Fine");
            JButton returnBtn = new JButton("Return Book");

            sidebar.add(search);
            sidebar.add(order);
            sidebar.add(borrow);
            sidebar.add(fine);
            sidebar.add(returnBtn);

            search.addActionListener(e -> searchForm());
            order.addActionListener(e -> orderForm());
            borrow.addActionListener(e -> borrowForm());
            fine.addActionListener(e -> calculateFine());
            returnBtn.addActionListener(e -> returnForm());
        }

        sidebar.add(logout);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);

        viewBooks.addActionListener(e -> showBooksTable());
        logout.addActionListener(e -> showLoginScreen());

        frame.revalidate();
        frame.repaint();
    }

    // ================= VIEW BOOKS =================
    private void showBooksTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Name", "Author", "Qty", "Price", "Borrow"}, 0);

        for (Book b : database.getAllBooks()) {
            model.addRow(new Object[]{
                    b.getName(), b.getAuthor(), b.getQTY(),
                    b.getPrice(), b.getBrwCopies()
            });
        }

        JTable table = new JTable(model);
        switchContent(new JScrollPane(table));
    }

    // ================= FORMS =================
    private void addBookForm() {
        JTextField name = new JTextField();
        JTextField author = new JTextField();
        JTextField qty = new JTextField();
        JTextField price = new JTextField();

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Name")); panel.add(name);
        panel.add(new JLabel("Author")); panel.add(author);
        panel.add(new JLabel("Qty")); panel.add(qty);
        panel.add(new JLabel("Price")); panel.add(price);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Add Book", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Book b = new Book(name.getText(), author.getText(), "", "",
                    Integer.parseInt(qty.getText()),
                    Double.parseDouble(price.getText()), 5);
            database.AddBook(b);
            showBooksTable();
        }
    }

    private void deleteBookForm() {
        String name = JOptionPane.showInputDialog("Book name:");
        int i = database.getBook(name);
        if (i != -1) {
            database.deleteBook(i);
            showBooksTable();
        }
    }

    private void searchForm() {
        String name = JOptionPane.showInputDialog("Book name:");
        int i = database.getBook(name);
        if (i != -1) {
            JOptionPane.showMessageDialog(frame, database.getBook(i));
        }
    }

    private void deleteAllData() {
        database.deleteAllData();
        showLoginScreen();
    }

    private void borrowForm() {
        String name = JOptionPane.showInputDialog("Book name:");
        int i = database.getBook(name);
        if (i == -1) return;

        Book book = database.getBook(i);
        LocalDate end = LocalDate.now().plusDays(7);

        book.setBrwCopies(book.getBrwCopies() - 1);
        database.borrowBook(new Borrowing(LocalDate.now(), end, book, currentUser), book, i);
        showBooksTable();
    }

    private void returnForm() {
        String name = JOptionPane.showInputDialog("Book name:");
        for (Borrowing b : database.getAllBorrowings()) {
            if (b.getUser().getName().equals(currentUser.getName())
                    && b.getBook().getName().equals(name)) {

                Book book = b.getBook();
                int i = database.getBook(name);
                book.setBrwCopies(book.getBrwCopies() + 1);
                database.returnBook(b, book, i);
                break;
            }
        }
        showBooksTable();
    }

    private void orderForm() {
        String name = JOptionPane.showInputDialog("Book name:");
        int i = database.getBook(name);
        if (i == -1) return;

        Book book = database.getBook(i);
        int q = Integer.parseInt(JOptionPane.showInputDialog("Quantity:"));

        book.setQTY(book.getQTY() - q);
        database.addOrder(new Order(book, currentUser, q * book.getPrice(), q), book, i);

        showBooksTable();
    }

    private void viewOrders() {
        JTextArea area = new JTextArea();
        for (Order o : database.getAllOrders()) {
            area.append(o + "\n\n");
        }
        switchContent(new JScrollPane(area));
    }

    private void viewBorrowings() {
        JTextArea area = new JTextArea();
        for (Borrowing b : database.getAllBorrowings()) {
            area.append(b + "\n\n");
        }
        switchContent(new JScrollPane(area));
    }

    // ================= FIXED FINE =================
    private void calculateFine() {
        StringBuilder sb = new StringBuilder();
        double total = 0;

        for (Borrowing b : database.getAllBorrowings()) {
            if (b.getUser().getName().equals(currentUser.getName())) {
                LocalDate finish = LocalDate.parse(b.getFinish());

                long daysLate = ChronoUnit.DAYS.between(finish, LocalDate.now());
                double fine = daysLate > 0 ? daysLate : 0;

                sb.append("Book: ").append(b.getBook().getName())
                        .append("\nFine: $").append(fine)
                        .append("\n\n");

                total += fine;
            }
        }

        sb.append("Total Fine: $").append(total);
        switchContent(new JScrollPane(new JTextArea(sb.toString())));
    }

    private void switchContent(Component c) {
        contentPanel.removeAll();
        contentPanel.add(c);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        new LibraryGUI();
    }
}