package movies.gallery.ims;

import javax.swing.JFrame;
import java.sql.*;

public class MoviesGalleryIMS {
    static final String JDBC_DRIVER = "org.sqlite.JDBC";
    static final String DB_URL = "jdbc:sqlite:movies_database.db";

    public MoviesGalleryIMS() {
        createSQLDB();
    }

    public void createSQLDB() {
        Connection conn = null;
        Statement stmt = null;
        String sql = null; // Declare the sql variable here
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL);

            DatabaseMetaData meta = conn.getMetaData();
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Categories'");
            if (!rs.next()) {
                System.out.println("Creating table Movies...");
                sql = "CREATE TABLE Movies ( " +
                        "movie_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT, " +
                        "category TEXT CHECK (category IN ('Adventure', 'Romantic', 'Comedy', 'Drama', 'Action')), " +
                        "length_hours REAL, " +
                        "num_actors INTEGER, " +
                        "producer_id INTEGER, " +
                        "FOREIGN KEY (producer_id) REFERENCES Producers(producer_id) " +
                        ")";
                stmt.executeUpdate(sql);
                System.out.println("Table Movies created successfully");
            } else {
                System.out.println("Table Movies already exists");
            }
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Producers'");

            if (!rs.next()) {
                System.out.println("Creating table Producers...");
                sql = "CREATE TABLE Producers ( " +
                        "producer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "full_name TEXT, " +
                        "phone_number TEXT " +
                        ")";
                stmt.executeUpdate(sql);
                System.out.println("Table Producers created successfully");
            } else {
                System.out.println("Table producers already exists");
            }

            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Admins'");

            if (!rs.next()) {
                System.out.println("Creating table Admins...");
                sql = "CREATE TABLE Admins ( " +
                        "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "full_name TEXT, " +
                        "username TEXT UNIQUE, " +
                        "password TEXT " +
                        ")";
                stmt.executeUpdate(sql);
                System.out.println("Table Admins created successfully");
            } else {
                System.out.println("Table Admins already exists");
            }
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'");

            if (!rs.next()) {
                System.out.println("Creating table users...");
                sql = "CREATE TABLE users ( " +
                        "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "full_name TEXT, " +
                        "username TEXT UNIQUE, " +
                        "password TEXT " +
                        ")";
                stmt.executeUpdate(sql);
                System.out.println("Table users created successfully");
            } else {
                System.out.println("Table users already exists");
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
           /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            newForm frame = new newForm();
            frame.setVisible(true);
        }
    });
    }
}
