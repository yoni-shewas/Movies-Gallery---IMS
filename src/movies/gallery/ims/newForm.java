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
                e.printStackTrace();
                // Load a default image
                ImageIcon defaultImageIcon = new ImageIcon(getClass().getResource("icon.png"));
                imageLabel = new JLabel(defaultImageIcon);
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
 ;
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
        jPanel3 = new javax.swing.JPanel();

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
                            .addComponent(UsernameSignup, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Fullname, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(label6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, signUPLayout.createSequentialGroup()
                                .addGap(87, 87, 87)
                                .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, signUPLayout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addGroup(signUPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
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

        javax.swing.GroupLayout HomePageLayout = new javax.swing.GroupLayout(HomePage);
        HomePage.setLayout(HomePageLayout);
        HomePageLayout.setHorizontalGroup(
            HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HomePageLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(UserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel2)
                        .addComponent(middleSection, javax.swing.GroupLayout.DEFAULT_SIZE, 1378, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addComponent(bottomSection, javax.swing.GroupLayout.DEFAULT_SIZE, 1378, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addComponent(topSection, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        HomePageLayout.setVerticalGroup(
            HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HomePageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(UserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addContainerGap(15, Short.MAX_VALUE))
        );

        tabs.addTab("Home", HomePage);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1440, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 755, Short.MAX_VALUE)
        );

        tabs.addTab("tab4", jPanel3);

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
            switchToHomePanel();

        } else {
            JOptionPane.showMessageDialog(null, "Failed to sign up user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_signUpButtonMouseClicked

    private void UsernameSignupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UsernameSignupActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UsernameSignupActionPerformed

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

    private void passwordSigninKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordSigninKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            signIn();
        }
    }//GEN-LAST:event_passwordSigninKeyPressed

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
    private javax.swing.JTextField Fullname;
    private javax.swing.JPanel HomePage;
    private javax.swing.JLabel Signup;
    private javax.swing.JLabel UserLabel;
    private javax.swing.JTextField UsernameSignin;
    private javax.swing.JTextField UsernameSignup;
    private javax.swing.JPanel bottomSection;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel3;
    private java.awt.Label label1;
    private java.awt.Label label2;
    private java.awt.Label label4;
    private java.awt.Label label5;
    private java.awt.Label label6;
    private java.awt.Label label7;
    private java.awt.Label label8;
    private java.awt.Label label9;
    private javax.swing.JPanel middleSection;
    private javax.swing.JPasswordField passwordSignin;
    private javax.swing.JPasswordField passwordSignup1;
    private javax.swing.JPasswordField passwordSignupConfirm;
    private javax.swing.JPanel signIn;
    private javax.swing.JButton signInButton;
    private javax.swing.JPanel signUP;
    private javax.swing.JButton signUpButton;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JPanel topSection;
    // End of variables declaration//GEN-END:variables
}
