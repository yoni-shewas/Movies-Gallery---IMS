/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package movies.gallery.ims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.*;
import java.awt.*;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import java.awt.Insets;
import javax.swing.border.Border;





/**
 *
 * @author PC
 */
public class newForm extends javax.swing.JFrame {

    /**
     * Creates new form newForm
     */
    static final String DB_URL = "jdbc:sqlite:movies_database.db";
    private JPanel moviePanel;
    private int userID;
    
    private JPanel[] topPanel = new JPanel[25];
    private JPanel[] middlePanel = new JPanel[25];
    private JPanel[] bottomPanel = new JPanel[25];
    
    int top = 0, middle = 0, bottom = 0;
    
    public newForm() {
        initComponents();
        switchToSignInPanel();
    }
    
   private void switchToSignInPanel() {
    // Set focus on the JTextArea
    UsernameSignin.requestFocusInWindow();
    tabs.setSelectedComponent(signIn);
}

    private void switchToSignUpPanel() {
        UsernameSignup.requestFocusInWindow();
        tabs.setSelectedComponent(signUP);
        
    }

    private void switchToHomePanel() {
        tabs.setSelectedComponent(HomePage);
        
        System.out.println("Switching to Home Panel...");
    
        moviePanel = new JPanel(new GridLayout(0, 3, 10, 10));// 3 columns grid layout
        loadMovies();
        

        System.out.println("Switched to Home Panel successfully.");
    }
    private void switchToAdminPanel() {
        tabs.setSelectedComponent(adminPage);
        
        System.out.println("Switching to admin Panel...");
    
       
        loadMovies();
        

        System.out.println("Switched to Home Panel successfully.");
    }
    
    private boolean insertUserIntoDatabase(String fullname, String username, String password) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    try {
        conn = DriverManager.getConnection(DB_URL);

        String sql = "INSERT INTO users (full_name, username, password) VALUES (?, ?, ?)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, fullname);
        pstmt.setString(2, username);
        pstmt.setString(3, password);

        int rowsInserted = pstmt.executeUpdate();
        return rowsInserted > 0;
    } catch (SQLException e) {
        if (e.getErrorCode() == 19) { // SQLite error code for unique constraint violation
            JOptionPane.showMessageDialog(null, "Username is already taken.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Failed to sign up user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    } finally {
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
   }

private void loadMovies() {
    

    // Load movies from the database and add them to the movie panel
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:movies_database.db");
         PreparedStatement pstmt = conn.prepareStatement("SELECT m.movie_id, m.title, m.image_path, m.category, m.length_hours, m.num_actors, m.producer_id, AVG(r.rating) AS avg_rating\n" +
                                                        "FROM movies m LEFT JOIN ratings r ON m.movie_id = r.movie_id\n" +
                                                        "GROUP BY m.movie_id, m.title, m.image_path, m.category, m.length_hours, m.num_actors, m.producer_id")) {

        ResultSet rs = pstmt.executeQuery();

        System.out.println("Loading movies...");

        while (rs.next()) {
            int id = rs.getInt("movie_id");
            String title = rs.getString("title");
            String imagePath = "/movies/gallery/ims/Movie_Images/" + rs.getString("image_path");
            String category = rs.getString("category");
            int lengthHours = rs.getInt("length_hours");
            int numActors = rs.getInt("num_actors");
            String producerId = rs.getString("producer_id");
            String producer = fetchProducerName(producerId);
            JLabel imageLabel = null;
            try {
                ImageIcon imageIcon = new ImageIcon(getClass().getResource(imagePath));
                Image scaledImage = imageIcon.getImage().getScaledInstance(145, 180, Image.SCALE_SMOOTH);
                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                imageLabel = new JLabel(scaledImageIcon);
                imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            } catch (NullPointerException e) {
                // Handle the case where the image file is not found
                System.err.println("Image file not found: " + e.getMessage());
                // Load a default image
                ImageIcon imageIcon = new ImageIcon(getClass().getResource("icon.png"));
                Image scaledImage = imageIcon.getImage().getScaledInstance(145, 180, Image.SCALE_SMOOTH);
                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                imageLabel = new JLabel(scaledImageIcon);
                imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
            }


            JLabel titleLabel = new JLabel(title);
            JLabel categoryLabel = new JLabel("Category: " + category);
            JLabel lengthLabel = new JLabel("Length (hours): " + lengthHours);
            JLabel actorsLabel = new JLabel("Number of Actors: " + numActors);
            JLabel producerLabel = new JLabel("Producer: " + producer);
            JLabel ratingLabel = new JLabel("Avg Rating: " + String.format("%.2f", rs.getDouble("avg_rating")));

            JButton rateButton = new JButton("Rate");
            rateButton.addActionListener(e -> {
                String ratingStr = JOptionPane.showInputDialog(null, "Enter your rating (1-5):");
                try {
                    int rating = Integer.parseInt(ratingStr);
                    if (rating >= 1 && rating <= 5) {
                        JOptionPane.showMessageDialog(null, "You rated '" + title + "' as " + rating);
                        rateMovie(id, userID, rating);
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid rating! Please enter a number between 1 and 5.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid rating! Please enter a valid number.");
                }
            });

            JPanel contentPanel = new JPanel(new BorderLayout());
            JLabel paddingLabel = new JLabel(" "); // Add padding
            contentPanel.add(paddingLabel, BorderLayout.NORTH);
            JPanel textPanel = new JPanel(new GridLayout(0, 1));
            textPanel.add(titleLabel);
            textPanel.add(categoryLabel);
            textPanel.add(lengthLabel);
            textPanel.add(actorsLabel);
            textPanel.add(producerLabel);
            textPanel.add(ratingLabel);
            JPanel ratingButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            ratingButtonPanel.add(rateButton);
            contentPanel.add(textPanel, BorderLayout.CENTER);
            contentPanel.add(ratingButtonPanel, BorderLayout.SOUTH);

            JPanel movieInfoPanel = new JPanel(); // Initialize inside the loop
            movieInfoPanel.setLayout(new BoxLayout(movieInfoPanel, BoxLayout.X_AXIS)); // Use BoxLayout for side-by-side alignment
            movieInfoPanel.setPreferredSize(new Dimension(250, 185)); // Set preferred size
            movieInfoPanel.setBorder(BorderFactory.createEmptyBorder()); // Ensure no extra space is taken up
            
            int leftPadding = 0; // Adjust this value as needed
            int topPadding = 0;
            int rightPadding = 12;
            int bottomPadding = 0;
            Border paddingBorder = BorderFactory.createEmptyBorder(topPadding, leftPadding, bottomPadding, rightPadding);

            // Add the padding border to the movieInfoPanel
            movieInfoPanel.setBorder(paddingBorder);

            movieInfoPanel.add(imageLabel);
            movieInfoPanel.add(contentPanel);

            JPanel section;
            
            switch (category) {
                case "Adventure":
                    section = topSection;
                    if (top < 4) {
                        section.add(movieInfoPanel);
                        topPanel[top] = movieInfoPanel;
                        top++;
                        if (top == 4) {
                            JButton moreButton = new JButton("more");
                            section.add(moreButton);
                            final int mutableTop = top;
                            moreButton.addActionListener(e -> {
                                moreMovie(category, mutableTop);
                            });
                        }
                        section.revalidate();
                        section.repaint();
                        
                    } else {
                        topPanel[top] = movieInfoPanel;
                        top++;
                    }
                    break;
                case "Comedy":
                    section = middleSection;
                    if (middle < 4) {
                        middlePanel[middle] = movieInfoPanel;
                        section.add(movieInfoPanel);
                         middle++;
                        if (middle == 4) {
                            JButton moreButton = new JButton("more");
                            section.add(moreButton);
                            final int mutableMiddle = middle;
                            moreButton.addActionListener(e -> {
                                moreMovie(category, mutableMiddle);
                            });
                        }
                        section.revalidate();
                        section.repaint();
                       
                    } else {
                        middlePanel[middle] = movieInfoPanel;
                        middle++;
                    }
                    break;
                case "Romantic":
                    section = bottomSection;
                   
                    if (bottom < 4) {
                        bottomPanel[bottom] = movieInfoPanel;
                        section.add(movieInfoPanel);
                        bottom++;
                        if (bottom == 4) {
                            JButton moreButton = new JButton("more");
                            section.add(moreButton);
                            final int mutableBottom = bottom;
                            moreButton.addActionListener(e -> {
                                moreMovie(category, mutableBottom);
                            });
                        }
                        section.revalidate();
                        section.repaint();
                        
                    } else {
                        bottomPanel[bottom] = movieInfoPanel;
                        bottom++;
                    }
                    break;
                default:
                    section = topSection; // Default to top section if category is not recognized
                    break;
            }
        }

        System.out.println("Movies loaded successfully.");
    } catch (SQLException ex) {
        System.err.println("Error loading movies: " + ex.getMessage());
        ex.printStackTrace();
    }
}


public void moreMovie(String category, int bottomIndex) {
    final String finalCategory = category; // Declare category as final

    JPanel section;
    JPanel[] panelArray;
    int panelArraySize = 0;
    System.out.println(bottomIndex);

    // Determine the section and corresponding panel array based on the category
    switch (category) {
        case "Adventure":
            section = topSection;
            panelArray = topPanel;
            panelArraySize = top;
            break;
        case "Comedy":
            section = middleSection;
            panelArray = middlePanel;
            panelArraySize = middle;
            break;
        case "Romantic":
            section = bottomSection;
            panelArray = bottomPanel;
            panelArraySize = bottom;
            break;
        default:
            section = topSection;
            panelArray = topPanel;
            break;
    }

    // Remove existing movie panels from the section
    section.removeAll();

    // Add saved data from the array (up to 4 items)
    int endIndex = Math.min(bottomIndex + 4, panelArraySize);
    for (int i = bottomIndex; i < endIndex; i++) {
        if (i < panelArray.length && panelArray[i] != null) { // Check if i is within array bounds
            section.add(panelArray[i]);
        }
    }
    
    JPanel morePanel = new JPanel(new BorderLayout());
    morePanel.setPreferredSize(new Dimension(120, 60));
        

    // Create a "more" button if there are any remaining items left in the array
    if (endIndex < panelArraySize) {
        
        JButton moreButton = new JButton("more");
        moreButton.setPreferredSize(new Dimension(120, 40));
        
        
        final int finalEndIndex = endIndex+4;
        moreButton.addActionListener(e -> {
            moreMovie(finalCategory, finalEndIndex);
        });
        
        JButton backButton = new JButton("back");
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.addActionListener(e -> {
            previousPage(finalCategory, bottomIndex);
        });
        
        morePanel.add(moreButton, BorderLayout.NORTH);
        morePanel.add(backButton, BorderLayout.SOUTH);
        
        section.add(morePanel);
    }
    else{
        JButton backButton = new JButton("back");
        backButton.setPreferredSize(new Dimension(120, 40)); // Set preferred size
        backButton.addActionListener(e -> {
            previousPage(finalCategory, bottomIndex);
        });

        morePanel.add(backButton, BorderLayout.CENTER);
        section.add(backButton);

    }

    // Revalidate and repaint the section to update the UI
    section.revalidate();
    section.repaint();
}


private void previousPage(String category, int bottomIndex) {
    final String finalCategory = category; // Declare category as final

    JPanel section;
    JPanel[] panelArray;
    int panelArraySize = 0;
    
    
    
    

    // Determine the section and corresponding panel array based on the category
    switch (category) {
        case "Adventure":
            section = topSection;
            panelArray = topPanel;
            panelArraySize = top;
            break;
        case "Comedy":
            section = middleSection;
            panelArray = middlePanel; 
            panelArraySize = middle;
            break;
        case "Romantic":
            section = bottomSection;
            panelArray = bottomPanel;
            panelArraySize = bottom;
            break;
        default:
            section = topSection;
            panelArray = topPanel;
            break;
    }

    // Remove existing movie panels from the section
    section.removeAll();

    // Calculate the starting index for the previous page
    int startIndex = bottomIndex - 4;
//    System.out.println("botttom I");
    
    

    // Add saved data from the array (up to 4 items)
    for (int i = startIndex; i < bottomIndex; i++) {
        if (i >= 0 && i < panelArraySize && panelArray[i] != null) { // Check if i is within array bounds
            section.add(panelArray[i]);
        }
    }

    // Create a "more" button if there are any remaining items left in the array
    if (startIndex >= 0) {
        JButton moreButton = new JButton("more");
        moreButton.setPreferredSize(new Dimension(120, 40));
        moreButton.addActionListener(e -> {
            moreMovie(finalCategory, bottomIndex);
        });
        section.add(moreButton);
        
        if(startIndex > 0){
            JButton backButton = new JButton("back");
            backButton.setPreferredSize(new Dimension(120, 40));
            backButton.addActionListener(e -> {
                previousPage(finalCategory, startIndex);
            });
            section.add(backButton);
        }
    }

    // Revalidate and repaint the section to update the UI
    section.revalidate();
    section.repaint();
}









private String fetchProducerName(String producerId) {
    String producerName = null;
    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement pstmt = conn.prepareStatement("SELECT full_name FROM producers WHERE producer_id = ?")) {
        pstmt.setString(1, producerId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            producerName = rs.getString("full_name");
        }
    } catch (SQLException ex) {
        System.err.println("Error fetching producer name: " + ex.getMessage());
        ex.printStackTrace();
    }
    return producerName;
}
private void userId(String username) {
    String producerName = null;
    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement pstmt = conn.prepareStatement("SELECT user_id FROM users WHERE username= ?")) {
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
           userID = rs.getInt("user_id");
        }
    } catch (SQLException ex) {
        System.err.println("Error fetching producer name: " + ex.getMessage());
        ex.printStackTrace();
    }
 
}

private String fetchProducerId(String producerName) {
    String producerId = null;
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:movies_database.db");
         PreparedStatement pstmt = conn.prepareStatement("SELECT producer_id FROM producers WHERE full_name = ?")) {
        pstmt.setString(1, producerName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            producerId = rs.getString("producer_id");
        }
    } catch (SQLException ex) {
        System.err.println("Error fetching producer ID: " + ex.getMessage());
        ex.printStackTrace();
    }
    return producerId;
}



// Function to save user rating to the database
private boolean rateMovie(int movieId, int userId, int rating) {
    // Check if the user has already rated the movie
    if (hasUserRatedMovie(movieId, userId)) {
        System.out.println("You have already rated this movie.");
        JOptionPane.showMessageDialog(null, "You have already rated this movie");
        return false;
    }

    Connection conn = null;
    PreparedStatement pstmt = null;
    try {
        conn = DriverManager.getConnection(DB_URL);
        String sql = "INSERT INTO ratings (movie_id, user_id, rating) VALUES (?, ?, ?)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, movieId);
        pstmt.setInt(2, userId);
        pstmt.setInt(3, rating);
        int rowsInserted = pstmt.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Rating saved successfully.");
            return true;
        } else {
            System.err.println("Failed to save user rating.");
            return false;
        }
    } catch (SQLException e) {
        System.err.println("Error saving user rating: " + e.getMessage());
        e.printStackTrace();
        return false;
    } finally {
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

private boolean hasUserRatedMovie(int movieId, int userId) {
    String sql = "SELECT COUNT(*) FROM ratings WHERE movie_id = ? AND user_id = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, movieId);
        pstmt.setInt(2, userId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);
            return count > 0;
        }
    } catch (SQLException e) {
        System.err.println("Error checking user rating: " + e.getMessage());
    }
    return false;
}

public void  signIn(){
        String username = UsernameSignin.getText();
        String password = new String(passwordSignin.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inputs can not be empty");
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:movies_database.db");

            String adminSql = "SELECT * FROM Admins WHERE username = ? AND password = ?";
            stmt = conn.prepareStatement(adminSql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Admin sign-in successful");
                AdminrLabel.setText(username);
                switchToAdminPanel();
                // Perform actions for admin
            } else {
                String userSql = "SELECT * FROM users WHERE username = ? AND password = ?";
                stmt = conn.prepareStatement(userSql);
                stmt.setString(1, username);
                stmt.setString(2, password);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    UserLabel.setText(username);
                    userId(username);
                    switchToHomePanel();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password");
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (rs != null)
                rs.close();
                if (stmt != null)
                stmt.close();
                if (conn != null)
                conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
}

private boolean isUsernameUnique(String username, String table) {
    String query = "SELECT COUNT(*) AS count FROM " + table + " WHERE username = ?";
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:movies_database.db");
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt("count");
            return count == 0; // If count is 0, the username is unique
        }
    } catch (SQLException ex) {
        System.err.println("Error checking username uniqueness: " + ex.getMessage());
        ex.printStackTrace();
    }
    // Error occurred or username exists, return false
    return false;
}


    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();
        signIn = new javax.swing.JPanel();
        UsernameSignin = new javax.swing.JTextField();
        label1 = new java.awt.Label();
        signInButton = new javax.swing.JButton();
        Signup = new javax.swing.JLabel();
        passwordSignin = new javax.swing.JPasswordField();
        label7 = new java.awt.Label();
        label8 = new java.awt.Label();
        signUP = new javax.swing.JPanel();
        label2 = new java.awt.Label();
        UsernameSignup = new javax.swing.JTextField();
        signUpButton = new javax.swing.JButton();
        passwordSignupConfirm = new javax.swing.JPasswordField();
        passwordSignup1 = new javax.swing.JPasswordField();
        label4 = new java.awt.Label();
        label5 = new java.awt.Label();
        label6 = new java.awt.Label();
        label9 = new java.awt.Label();
        Fullname = new javax.swing.JTextField();
        HomePage = new javax.swing.JPanel();
        UserLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        topSection = new javax.swing.JPanel();
        middleSection = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        bottomSection = new javax.swing.JPanel();
        logout1 = new javax.swing.JLabel();
        adminPage = new javax.swing.JPanel();
        AdminrLabel = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        topSection1 = new javax.swing.JPanel();
        middleSection2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        movieName = new javax.swing.JTextField();
        movieLength = new javax.swing.JTextField();
        movieCategory = new javax.swing.JComboBox<>();
        movieNumberOfActors = new javax.swing.JTextField();
        movieProducer = new javax.swing.JComboBox<>();
        addMovies = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        movieImagePath = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        adminFullName = new javax.swing.JTextField();
        addAdmin = new javax.swing.JButton();
        adminUsername = new javax.swing.JTextField();
        adminPassword = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        label10 = new java.awt.Label();
        label11 = new java.awt.Label();
        label12 = new java.awt.Label();
        logout = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        signIn.setBackground(new java.awt.Color(36, 37, 41));
        signIn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        signIn.setForeground(new java.awt.Color(254, 255, 255));

        UsernameSignin.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        UsernameSignin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UsernameSigninActionPerformed(evt);
            }
        });

        label1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        label1.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        label1.setForeground(new java.awt.Color(216, 141, 0));
        label1.setMinimumSize(new java.awt.Dimension(70, 200));
        label1.setName(""); // NOI18N
        label1.setPreferredSize(new java.awt.Dimension(1009, 2099));
        label1.setText("Sign In");

        signInButton.setBackground(new java.awt.Color(216, 141, 0));
        signInButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        signInButton.setText("Sign In");
        signInButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signInButtonMouseClicked(evt);
            }
        });
        signInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signInButtonActionPerformed(evt);
            }
        });

        Signup.setBackground(new java.awt.Color(0, 0, 0));
        Signup.setForeground(new java.awt.Color(216, 141, 0));
        Signup.setText("Don't have an Account? Sign Up");
        Signup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SignupMouseClicked(evt);
            }
        });

        passwordSignin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passwordSigninKeyPressed(evt);
            }
        });

        label7.setForeground(new java.awt.Color(255, 255, 255));
        label7.setText("Username");

        label8.setForeground(new java.awt.Color(255, 255, 255));
        label8.setText("Password");

        javax.swing.GroupLayout signInLayout = new javax.swing.GroupLayout(signIn);
        signIn.setLayout(signInLayout);
        signInLayout.setHorizontalGroup(
            signInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signInLayout.createSequentialGroup()
                .addGap(524, 524, 524)
                .addGroup(signInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(signInLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(signInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(signInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(UsernameSignin, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(signInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Signup, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(passwordSignin, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, signInLayout.createSequentialGroup()
                                    .addGap(109, 109, 109)
                                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(signInLayout.createSequentialGroup()
                        .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 294, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(722, 722, 722))
        );
        signInLayout.setVerticalGroup(
            signInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signInLayout.createSequentialGroup()
                .addGap(177, 177, 177)
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UsernameSignin, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(passwordSignin, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(signInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Signup, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabs.addTab("signIN", signIn);

        signUP.setBackground(new java.awt.Color(36, 37, 41));
        signUP.setForeground(new java.awt.Color(245, 245, 245));
        signUP.setAutoscrolls(true);

        label2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        label2.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        label2.setForeground(new java.awt.Color(216, 141, 0));
        label2.setMinimumSize(new java.awt.Dimension(70, 200));
        label2.setName(""); // NOI18N
        label2.setPreferredSize(new java.awt.Dimension(1009, 2099));
        label2.setText("Sign Up");

        UsernameSignup.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        UsernameSignup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UsernameSignupActionPerformed(evt);
            }
        });

        signUpButton.setBackground(new java.awt.Color(216, 141, 0));
        signUpButton.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        signUpButton.setText("Sign Up");
        signUpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                signUpButtonMouseClicked(evt);
            }
        });
        signUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signUpButtonActionPerformed(evt);
            }
        });

        passwordSignup1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordSignup1ActionPerformed(evt);
            }
        });

        label4.setForeground(new java.awt.Color(255, 255, 255));
        label4.setText("Confirm Password");

        label5.setForeground(new java.awt.Color(255, 255, 255));
        label5.setText("Password");

        label6.setForeground(new java.awt.Color(255, 255, 255));
        label6.setText("Username");

        label9.setForeground(new java.awt.Color(255, 255, 255));
        label9.setText("FullName");

        Fullname.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Fullname.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FullnameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout signUPLayout = new javax.swing.GroupLayout(signUP);
        signUP.setLayout(signUPLayout);
        signUPLayout.setHorizontalGroup(
            signUPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signUPLayout.createSequentialGroup()
                .addGap(546, 546, 546)
                .addGroup(signUPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(passwordSignup1)
                    .addComponent(passwordSignupConfirm)
                    .addComponent(signUpButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(signUPLayout.createSequentialGroup()
                        .addGroup(signUPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(UsernameSignup, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, signUPLayout.createSequentialGroup()
                                .addGap(87, 87, 87)
                                .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, signUPLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addGroup(signUPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(Fullname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(634, 634, 634))
        );
        signUPLayout.setVerticalGroup(
            signUPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(signUPLayout.createSequentialGroup()
                .addGap(140, 140, 140)
                .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(label9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Fullname, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UsernameSignup, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordSignup1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordSignupConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(signUpButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabs.addTab("SignUP", signUP);

        HomePage.setBackground(new java.awt.Color(36, 37, 41));
        HomePage.setForeground(new java.awt.Color(245, 245, 245));
        HomePage.setFocusTraversalPolicyProvider(true);

        UserLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        UserLabel.setForeground(new java.awt.Color(216, 141, 0));
        UserLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/movies/gallery/ims/UserIcon.png"))); // NOI18N
        UserLabel.setText("User");
        UserLabel.setToolTipText("");

        jLabel1.setForeground(new java.awt.Color(216, 141, 0));
        jLabel1.setText("Adventure");

        topSection.setPreferredSize(new java.awt.Dimension(1000000, 200));
        topSection.setLayout(new javax.swing.BoxLayout(topSection, javax.swing.BoxLayout.LINE_AXIS));

        middleSection.setLayout(new javax.swing.BoxLayout(middleSection, javax.swing.BoxLayout.LINE_AXIS));

        jLabel2.setForeground(new java.awt.Color(216, 141, 0));
        jLabel2.setText("Comedy");

        jLabel3.setForeground(new java.awt.Color(216, 141, 0));
        jLabel3.setText("Romantic");

        bottomSection.setLayout(new javax.swing.BoxLayout(bottomSection, javax.swing.BoxLayout.LINE_AXIS));

        logout1.setFont(new java.awt.Font("Segoe UI Black", 1, 17)); // NOI18N
        logout1.setForeground(new java.awt.Color(255, 255, 255));
        logout1.setText("Logout");
        logout1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logout1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout HomePageLayout = new javax.swing.GroupLayout(HomePage);
        HomePage.setLayout(HomePageLayout);
        HomePageLayout.setHorizontalGroup(
            HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HomePageLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2)
                    .addComponent(middleSection, javax.swing.GroupLayout.DEFAULT_SIZE, 1378, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addComponent(bottomSection, javax.swing.GroupLayout.DEFAULT_SIZE, 1378, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(topSection, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HomePageLayout.createSequentialGroup()
                        .addComponent(UserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(logout1)))
                .addContainerGap(433, Short.MAX_VALUE))
        );
        HomePageLayout.setVerticalGroup(
            HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HomePageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logout1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topSection, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(middleSection, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomSection, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        tabs.addTab("Home", HomePage);

        adminPage.setBackground(new java.awt.Color(36, 37, 41));
        adminPage.setForeground(new java.awt.Color(245, 245, 245));
        adminPage.setFocusTraversalPolicyProvider(true);

        AdminrLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        AdminrLabel.setForeground(new java.awt.Color(216, 141, 0));
        AdminrLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/movies/gallery/ims/UserIcon.png"))); // NOI18N
        AdminrLabel.setText("User");
        AdminrLabel.setToolTipText("");

        jLabel7.setForeground(new java.awt.Color(216, 141, 0));
        jLabel7.setText("Adventure");

        topSection1.setPreferredSize(new java.awt.Dimension(1000000, 200));
        topSection1.setLayout(new javax.swing.BoxLayout(topSection1, javax.swing.BoxLayout.LINE_AXIS));

        middleSection2.setLayout(new javax.swing.BoxLayout(middleSection2, javax.swing.BoxLayout.LINE_AXIS));

        jLabel8.setForeground(new java.awt.Color(216, 141, 0));
        jLabel8.setText("Comedy");

        movieName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movieNameActionPerformed(evt);
            }
        });

        movieCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Adventure", "Comedy", "Romantic" }));

        movieProducer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Jhon", "Linda", "Cameron" }));

        addMovies.setBackground(new java.awt.Color(216, 141, 0));
        addMovies.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addMovies.setText("Add");
        addMovies.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addMoviesMouseClicked(evt);
            }
        });
        addMovies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMoviesActionPerformed(evt);
            }
        });

        jLabel9.setForeground(new java.awt.Color(216, 141, 0));
        jLabel9.setText("Title");

        jLabel10.setForeground(new java.awt.Color(216, 141, 0));
        jLabel10.setText("Categories");

        jLabel11.setForeground(new java.awt.Color(216, 141, 0));
        jLabel11.setText("Producers");

        jLabel12.setForeground(new java.awt.Color(216, 141, 0));
        jLabel12.setText("number of actors");

        jLabel13.setForeground(new java.awt.Color(216, 141, 0));
        jLabel13.setText("Movie Length");

        jLabel14.setForeground(new java.awt.Color(216, 141, 0));
        jLabel14.setText("Poster Image Path( with full name)");

        jLabel15.setForeground(new java.awt.Color(216, 141, 0));
        jLabel15.setText("Add Movies");

        jLabel16.setForeground(new java.awt.Color(216, 141, 0));
        jLabel16.setText("Add Admin");

        addAdmin.setBackground(new java.awt.Color(216, 141, 0));
        addAdmin.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        addAdmin.setText("Add Admin");
        addAdmin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addAdminMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addAdminMouseEntered(evt);
            }
        });
        addAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAdminActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        label10.setForeground(new java.awt.Color(255, 255, 255));
        label10.setText("FullName");

        label11.setForeground(new java.awt.Color(255, 255, 255));
        label11.setText("Username");

        label12.setForeground(new java.awt.Color(255, 255, 255));
        label12.setText("password");

        logout.setFont(new java.awt.Font("Segoe UI Black", 1, 17)); // NOI18N
        logout.setForeground(new java.awt.Color(255, 255, 255));
        logout.setText("Logout");
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout adminPageLayout = new javax.swing.GroupLayout(adminPage);
        adminPage.setLayout(adminPageLayout);
        adminPageLayout.setHorizontalGroup(
            adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, adminPageLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, adminPageLayout.createSequentialGroup()
                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, adminPageLayout.createSequentialGroup()
                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(adminPageLayout.createSequentialGroup()
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(movieName, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(adminPageLayout.createSequentialGroup()
                                        .addGap(205, 205, 205)
                                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(movieLength, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(39, 39, 39)
                                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(movieImagePath, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel14)
                                            .addGroup(adminPageLayout.createSequentialGroup()
                                                .addGap(58, 58, 58)
                                                .addComponent(addMovies, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, adminPageLayout.createSequentialGroup()
                                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(adminPageLayout.createSequentialGroup()
                                                .addComponent(movieCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(49, 49, 49)
                                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(movieNumberOfActors, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(39, 39, 39)
                                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(movieProducer, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(26, 26, 26)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 294, Short.MAX_VALUE)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(105, 105, 105)
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(adminPageLayout.createSequentialGroup()
                                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(addAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(adminPassword)
                                            .addComponent(adminUsername)
                                            .addComponent(adminFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(286, 286, 286))
                                    .addGroup(adminPageLayout.createSequentialGroup()
                                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 402, Short.MAX_VALUE)
                                        .addComponent(AdminrLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(logout))))
                            .addComponent(topSection1, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(middleSection2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(45, 45, 45))))
        );
        adminPageLayout.setVerticalGroup(
            adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(adminPageLayout.createSequentialGroup()
                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(adminPageLayout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(adminPageLayout.createSequentialGroup()
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel13))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(adminPageLayout.createSequentialGroup()
                                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(movieName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(movieLength, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(movieImagePath, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel10)
                                            .addComponent(jLabel12)
                                            .addComponent(jLabel11))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(movieCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(movieNumberOfActors, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(movieProducer, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(16, 16, 16)
                                        .addComponent(addMovies))
                                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                .addComponent(jLabel7))
                            .addGroup(adminPageLayout.createSequentialGroup()
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(adminFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(adminUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(16, 16, 16)
                                .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(label12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(adminPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(addAdmin)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(adminPageLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(adminPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(AdminrLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(logout, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(topSection1, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(20, 20, 20)
                .addComponent(middleSection2, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabs.addTab("Admin", adminPage);

        getContentPane().add(tabs, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1440, 790));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void FullnameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FullnameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FullnameActionPerformed

    private void passwordSignup1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordSignup1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordSignup1ActionPerformed

    private void signUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signUpButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_signUpButtonActionPerformed

    private void signUpButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signUpButtonMouseClicked
        // TODO add your handling code here:
        // TODO add your handling code here:
        String fullname = Fullname.getText();
        String username = UsernameSignup.getText();
        String password = new String(passwordSignup1.getPassword());
        String confirmPassword = new String(passwordSignupConfirm.getPassword());

        // Check if any field is empty
        if (fullname.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if password and confirmed password match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insert the new user into the database
        if (insertUserIntoDatabase(fullname, username, password)) {
            JOptionPane.showMessageDialog(null, "User signed up successfully.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            userId(username);
            UserLabel.setText(username);
            switchToHomePanel();

        } else {
            JOptionPane.showMessageDialog(null, "Failed to sign up user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_signUpButtonMouseClicked

    private void UsernameSignupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UsernameSignupActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UsernameSignupActionPerformed

    private void passwordSigninKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordSigninKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            signIn();
        }
    }//GEN-LAST:event_passwordSigninKeyPressed

    private void SignupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SignupMouseClicked
        // TODO add your handling code here:
        switchToSignUpPanel();
    }//GEN-LAST:event_SignupMouseClicked

    private void signInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signInButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_signInButtonActionPerformed

    private void signInButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_signInButtonMouseClicked
        // TODO add your handling code here:
        signIn();
    }//GEN-LAST:event_signInButtonMouseClicked

    private void UsernameSigninActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UsernameSigninActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UsernameSigninActionPerformed

    private void addMoviesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addMoviesMouseClicked
        // TODO add your handling code here:
            String name = movieName.getText();
            String movieLengthStr = movieLength.getText();
            String numActorsStr = movieNumberOfActors.getText();
            String imagePath = movieImagePath.getText();
            String category = (String) movieCategory.getSelectedItem();
            String producer = (String) movieProducer.getSelectedItem();
            

            // Check if any required fields are empty
            if (name.isEmpty() || movieLengthStr.isEmpty() || numActorsStr.isEmpty() || imagePath.isEmpty() || category == null || producer == null) {
                // Display an error message or handle the empty fields accordingly
                JOptionPane.showMessageDialog(null, "Please fill in all fields.");
                return; // Exit the method if any required fields are empty
            }

            // Validate numeric inputs
            int movieLength;
            int numActors;
            try {
                movieLength = Integer.parseInt(movieLengthStr);
                numActors = Integer.parseInt(numActorsStr);
                if(movieLength<0&&numActors<0){
                    JOptionPane.showMessageDialog(null, "Please enter a valid number for movie length and number of actors.");
                    return;
                }
            } catch (NumberFormatException ex) {
                // Handle the case where non-numeric input is entered
                JOptionPane.showMessageDialog(null, "Please enter a valid number for movie length and number of actors.");
                return; // Exit the method if numeric validation fails
            }

        // Perform validation here if needed

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:movies_database.db");
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO movies (title, length_hours, category, num_actors, producer_id, image_path) VALUES (?, ?, ?, ?, ?, ?)")) {
            String producerId = fetchProducerId(producer);
            producer = producerId;
            pstmt.setString(1, name);
            pstmt.setString(2, movieLengthStr);
            pstmt.setString(3, category);
            pstmt.setString(4, numActorsStr);
            pstmt.setString(5, producer);
            pstmt.setString(6, imagePath);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Movie added successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add movie.");
            }
        } catch (SQLException ex) {
            System.err.println("Error adding movie: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding movie: " + ex.getMessage());
        }
    }//GEN-LAST:event_addMoviesMouseClicked

    private void addMoviesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMoviesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addMoviesActionPerformed

    private void addAdminMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addAdminMouseClicked
        String fullName = adminFullName.getText();
        String username = adminUsername.getText();
        String password = adminPassword.getText();

        // Check if any required fields are empty
        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            // Display an error message or handle the empty fields accordingly
            JOptionPane.showMessageDialog(null, "Please fill in all fields.");
            return; // Exit the method if any required fields are empty
        }


        // Check if the username already exists in the admin table
        if (isUsernameUnique(username, "admins")) {
            // Check if the username already exists in the user table
            if (isUsernameUnique(username, "users")) {
                // Both admin and user tables don't have the username, proceed with insertion
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:movies_database.db");
                     PreparedStatement pstmt = conn.prepareStatement("INSERT INTO admins (full_name, username, password) VALUES (?, ?, ?)")) {

                    pstmt.setString(1, fullName);
                    pstmt.setString(2, username);
                    pstmt.setString(3, password);

                    int rowsInserted = pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        JOptionPane.showMessageDialog(null, "Admin added successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to add admin.");
                    }
                } catch (SQLException ex) {
                    System.err.println("Error adding admin: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error adding admin: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Username already exists in user database.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Username already exists in admin database.");
        }

    }//GEN-LAST:event_addAdminMouseClicked

    private void addAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAdminActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addAdminActionPerformed

    private void addAdminMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addAdminMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_addAdminMouseEntered

    private void movieNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movieNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_movieNameActionPerformed

    private void logoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutMouseClicked
        // TODO add your handling code here:
         int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
         if (choice == JOptionPane.YES_OPTION) {
        // User confirmed logout, switch to login screen
            switchToSignInPanel();
        }
    }//GEN-LAST:event_logoutMouseClicked

    private void logout1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logout1MouseClicked
        // TODO add your handling code here:
         int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
         if (choice == JOptionPane.YES_OPTION) {
        // User confirmed logout, switch to login screen
            switchToSignInPanel();
        }
    }//GEN-LAST:event_logout1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(newForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(newForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(newForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(newForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new newForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel AdminrLabel;
    private javax.swing.JTextField Fullname;
    private javax.swing.JPanel HomePage;
    private javax.swing.JLabel Signup;
    private javax.swing.JLabel UserLabel;
    private javax.swing.JTextField UsernameSignin;
    private javax.swing.JTextField UsernameSignup;
    private javax.swing.JButton addAdmin;
    private javax.swing.JButton addMovies;
    private javax.swing.JTextField adminFullName;
    private javax.swing.JPanel adminPage;
    private javax.swing.JTextField adminPassword;
    private javax.swing.JTextField adminUsername;
    private javax.swing.JPanel bottomSection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private java.awt.Label label1;
    private java.awt.Label label10;
    private java.awt.Label label11;
    private java.awt.Label label12;
    private java.awt.Label label2;
    private java.awt.Label label4;
    private java.awt.Label label5;
    private java.awt.Label label6;
    private java.awt.Label label7;
    private java.awt.Label label8;
    private java.awt.Label label9;
    private javax.swing.JLabel logout;
    private javax.swing.JLabel logout1;
    private javax.swing.JPanel middleSection;
    private javax.swing.JPanel middleSection2;
    private javax.swing.JComboBox<String> movieCategory;
    private javax.swing.JTextField movieImagePath;
    private javax.swing.JTextField movieLength;
    private javax.swing.JTextField movieName;
    private javax.swing.JTextField movieNumberOfActors;
    private javax.swing.JComboBox<String> movieProducer;
    private javax.swing.JPasswordField passwordSignin;
    private javax.swing.JPasswordField passwordSignup1;
    private javax.swing.JPasswordField passwordSignupConfirm;
    private javax.swing.JPanel signIn;
    private javax.swing.JButton signInButton;
    private javax.swing.JPanel signUP;
    private javax.swing.JButton signUpButton;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JPanel topSection;
    private javax.swing.JPanel topSection1;
    // End of variables declaration//GEN-END:variables
}
