package com.adrino.mlstudio.app;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


abstract class Product {
    protected int id;
    protected String name;
    protected double price;

    public Product(int id, String name, double price) {
        this.id = id; this.name = name; this.price = price;
    }

    public abstract String getCategory();
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return id + " | " + name + " | ₹" + price + " | " + getCategory();
    }
}

class PhysicalProduct extends Product {
    private int stock;

    public PhysicalProduct(int id, String name, double price, int stock) {
        super(id, name, price);
        this.stock = stock;
    }

    public int getStock() { return stock; }
    @Override public String getCategory() { return "Physical"; }
}

class DigitalProduct extends Product {
    private double sizeMB;

    public DigitalProduct(int id, String name, double price, double sizeMB) {
        super(id, name, price);
        this.sizeMB = sizeMB;
    }

    @Override public String getCategory() { return "Digital"; }
}

class GenericStore<T> {
    private List<T> items = new ArrayList<>();

    public void add(T item) { items.add(item); }
    public void remove(T item) { items.remove(item); }
    public void display() { items.forEach(System.out::println); }
    public List<T> getItems() { return items; }
}

@FunctionalInterface
interface Criteria<T> {
    boolean test(T t);
}


class ProductProcessor<T extends Product> {
    public void validate(T product) {
        System.out.println(product.getPrice() > 0 ? "Valid Product" : "Invalid Product");
    }

    public void compare(T p1, T p2) {
        System.out.println(p1.getPrice() > p2.getPrice() ? p1.name + " is costlier" : p2.name + " is costlier");
    }
}


class GenericUtil {
    public static <T> void displayCollection(Collection<T> items) {
        items.forEach(System.out::println);
    }
}


@FunctionalInterface
interface Arithmetic {
    double operate(double a, double b);
}

@FunctionalInterface
interface StringOp {
    String apply(String s);
}

@FunctionalInterface
interface Check {
    boolean test(int n);
}


public class MLStudioGenericsDemo extends Application {
    static final String URL = "jdbc:mysql://localhost:3306/storedb";
    static final String USER = "root";
    static final String PASS = "password";

    ResultSet rs;
    Statement stmt;

    TextField idField = new TextField();
    TextField nameField = new TextField();
    TextField priceField = new TextField();
    ImageView imageView = new ImageView();

    public static void main(String[] args) {

        GenericStore<Product> store = new GenericStore<>();
        store.add(new PhysicalProduct(1, "Laptop", 70000, 5));
        store.add(new DigitalProduct(2, "E-Book", 500, 2.5));
        store.display();

        ProductProcessor<Product> processor = new ProductProcessor<>();
        processor.validate(store.getItems().get(0));

        GenericUtil.displayCollection(store.getItems());

        Criteria<Product> premium = p -> p.getPrice() > 10000;
        store.getItems().stream().filter(premium::test).forEach(System.out::println);


        Arithmetic add = (a,b) -> a+b;
        Arithmetic div = (a,b) -> a/b;

        StringOp reverse = s -> new StringBuilder(s).reverse().toString();
        StringOp vowels = s -> String.valueOf(s.chars().filter(c -> "aeiouAEIOU".indexOf(c)>=0).count());
        StringOp upper = String::toUpperCase;

        Check isEven = n -> n % 2 == 0;
        Check isPrime = n -> n > 1 && IntStream.range(2,n).noneMatch(i -> n%i==0);

        List<Integer> nums = Arrays.asList(2,3,5,7,8,10,10,3);

        nums.stream()
                .filter(isEven::test)
                .distinct()
                .sorted()
                .limit(3)
                .forEach(System.out::println);

        long count = nums.stream().count();
        int max = nums.stream().max(Integer::compare).get();

        System.out.println("Count=" + count + ", Max=" + max);

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        connectDB();

        Button first = new Button("First");
        Button next = new Button("Next");
        Button prev = new Button("Previous");
        Button last = new Button("Last");
        Button update = new Button("Update Price");

        first.setOnAction(e -> moveFirst());
        next.setOnAction(e -> moveNext());
        prev.setOnAction(e -> movePrev());
        last.setOnAction(e -> moveLast());
        update.setOnAction(e -> updatePrice());

        VBox form = new VBox(10,
                new Label("ID"), idField,
                new Label("Name"), nameField,
                new Label("Price"), priceField,
                imageView,
                new HBox(10, first, prev, next, last, update)
        );

        stage.setScene(new Scene(form, 400, 500));
        stage.setTitle("Product Manager");
        stage.show();

        moveFirst();
    }

    void connectDB() throws Exception {
        Connection con = DriverManager.getConnection(URL, USER, PASS);
        stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

        rs = stmt.executeQuery(
                "SELECT p.id, p.name, p.price, i.image " +
                        "FROM products p JOIN product_images i ON p.id=i.product_id"
        );

        ResultSetMetaData meta = rs.getMetaData();
        System.out.println("Columns: " + meta.getColumnCount());
        for (int i=1;i<=meta.getColumnCount();i++)
            System.out.println(meta.getColumnName(i) + " - " + meta.getColumnTypeName(i));
    }

    void showRecord() throws Exception {
        idField.setText(rs.getString("id"));
        nameField.setText(rs.getString("name"));
        priceField.setText(rs.getString("price"));

        Blob blob = rs.getBlob("image");
        if (blob != null) {
            Image img = new Image(blob.getBinaryStream());
            imageView.setImage(img);
            imageView.setFitWidth(150);
            imageView.setPreserveRatio(true);
        }
    }

    void moveFirst() { try { rs.first(); showRecord(); } catch(Exception e){} }
    void moveNext()  { try { if(!rs.isLast()) rs.next(); showRecord(); } catch(Exception e){} }
    void movePrev()  { try { if(!rs.isFirst()) rs.previous(); showRecord(); } catch(Exception e){} }
    void moveLast()  { try { rs.last(); showRecord(); } catch(Exception e){} }

    void updatePrice() {
        try {
            rs.updateDouble("price", Double.parseDouble(priceField.getText()));
            rs.updateRow();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
