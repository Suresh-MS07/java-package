import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple, secure GUI-based password generator in Java using Swing.
 */
public class PasswordGenerator extends JFrame {

    // Character sets to be used in password generation
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+<>?";

    private JSpinner lengthSpinner;
    private JCheckBox uppercaseCheckBox;
    private JCheckBox numbersCheckBox;
    private JCheckBox symbolsCheckBox;
    private JTextField passwordField;
    private JProgressBar strengthBar;
    private JLabel strengthLabel;

    public PasswordGenerator() {
        // --- Frame Setup ---
        setTitle("Java Password Generator");
        setSize(450, 380); // Increased height for new components
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new GridBagLayout());
        
        // --- UI Components ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Padding between components
        gbc.anchor = GridBagConstraints.WEST;

        // Length Label and Spinner
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Password Length:"), gbc);

        gbc.gridx = 1;
        SpinnerModel spinnerModel = new SpinnerNumberModel(12, 4, 128, 1); // value, min, max, step
        lengthSpinner = new JSpinner(spinnerModel);
        add(lengthSpinner, gbc);

        // Checkboxes for character types
        uppercaseCheckBox = new JCheckBox("Include Uppercase (A-Z)");
        uppercaseCheckBox.setSelected(true);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Span across two columns
        add(uppercaseCheckBox, gbc);
        
        numbersCheckBox = new JCheckBox("Include Numbers (0-9)");
        numbersCheckBox.setSelected(true);
        gbc.gridy = 2;
        add(numbersCheckBox, gbc);

        symbolsCheckBox = new JCheckBox("Include Symbols (!@#$...)");
        symbolsCheckBox.setSelected(true);
        gbc.gridy = 3;
        add(symbolsCheckBox, gbc);

        // Generate Button
        JButton generateButton = new JButton("Generate Password");
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        add(generateButton, gbc);

        // Password Display Field
        passwordField = new JTextField(25);
        passwordField.setEditable(false);
        passwordField.setFont(new Font("Monospaced", Font.BOLD, 14));
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(passwordField, gbc);

        // Copy Button
        JButton copyButton = new JButton("Copy to Clipboard");
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        add(copyButton, gbc);

        // --- Strength Meter ---
        strengthLabel = new JLabel("Strength:");
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        add(strengthLabel, gbc);

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setValue(0);
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(strengthBar, gbc);

        // --- Action Listeners ---
        generateButton.addActionListener(e -> generateAndDisplayPassword());
        copyButton.addActionListener(e -> copyPasswordToClipboard());
    }

    private void generateAndDisplayPassword() {
        if (!uppercaseCheckBox.isSelected() && !numbersCheckBox.isSelected() && !symbolsCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(this, 
                "You must select at least one character type (uppercase, numbers, or symbols).",
                "Invalid Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int length = (int) lengthSpinner.getValue();
        boolean useUppercase = uppercaseCheckBox.isSelected();
        boolean useNumbers = numbersCheckBox.isSelected();
        boolean useSymbols = symbolsCheckBox.isSelected();
        
        String password = generatePassword(length, useUppercase, useNumbers, useSymbols);
        passwordField.setText(password);
        updatePasswordStrength(password);
    }
    
    private void copyPasswordToClipboard() {
        String password = passwordField.getText();
        if (password != null && !password.isEmpty()) {
            StringSelection stringSelection = new StringSelection(password);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(this, "Password copied to clipboard!");
        } else {
            JOptionPane.showMessageDialog(this, "Nothing to copy. Generate a password first.", "Empty Field", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Calculates the strength of a given password and updates the GUI progress bar and label.
     * @param password The password to analyze.
     */
    private void updatePasswordStrength(String password) {
        int score = calculatePasswordStrength(password);
        strengthBar.setValue(score);

        if (score < 20) {
            strengthLabel.setText("Strength: Very Weak");
            strengthLabel.setForeground(Color.RED);
            strengthBar.setForeground(Color.RED);
        } else if (score < 40) {
            strengthLabel.setText("Strength: Weak");
            strengthLabel.setForeground(new Color(255, 120, 0)); // Orange
            strengthBar.setForeground(new Color(255, 120, 0));
        } else if (score < 60) {
            strengthLabel.setText("Strength: Medium");
            strengthLabel.setForeground(Color.ORANGE.darker());
            strengthBar.setForeground(Color.ORANGE.darker());
        } else if (score < 80) {
            strengthLabel.setText("Strength: Strong");
            strengthLabel.setForeground(new Color(0, 150, 0)); // Dark Green
            strengthBar.setForeground(new Color(0, 150, 0));
        } else {
            strengthLabel.setText("Strength: Very Strong");
            strengthLabel.setForeground(new Color(0, 100, 0)); // Darkest Green
            strengthBar.setForeground(new Color(0, 100, 0));
        }
    }

    /**
     * Scores a password based on length and character variety.
     * @param password The password string.
     * @return An integer score from 0 to 100.
     */
    private int calculatePasswordStrength(String password) {
        int score = 0;
        int length = password.length();

        // Very low score for short passwords
        if (length < 8) {
            return Math.max(length * 5, 0);
        }

        // 1. Base score for length (up to 40 points)
        score += Math.min(length * 3, 40);

        // 2. Check for character variety using regex
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()\\-_=+<>?].*");
        
        int typesCount = 0;
        if(hasUpper) typesCount++;
        if(hasNumber) typesCount++;
        if(hasSymbol) typesCount++;

        // 3. Add points for variety (up to 30 points)
        score += typesCount * 10;

        // 4. Bonus points for having a good mix
        if (typesCount >= 2 && length >= 10) {
            score += 15;
        }
        if (typesCount == 3 && length >= 12) {
            score += 15;
        }

        return Math.min(score, 100);
    }

    public static String generatePassword(int length, boolean useUppercase, boolean useNumbers, boolean useSymbols) {
        SecureRandom random = new SecureRandom();
        List<Character> passwordChars = new ArrayList<>();
        StringBuilder allowedChars = new StringBuilder(LOWERCASE);

        passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));

        if (useUppercase) {
            allowedChars.append(UPPERCASE);
            passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (useNumbers) {
            allowedChars.append(NUMBERS);
            passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (useSymbols) {
            allowedChars.append(SYMBOLS);
            passwordChars.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }

        for (int i = passwordChars.size(); i < length; i++) {
            int randomIndex = random.nextInt(allowedChars.length());
            passwordChars.add(allowedChars.charAt(randomIndex));
        }

        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    /**
     * The main method to run the password generator application.
     * It creates and shows the GUI.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure the GUI is created on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            PasswordGenerator generator = new PasswordGenerator();
            generator.setVisible(true);
        });
    }
}


