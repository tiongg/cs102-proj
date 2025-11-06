package g1t1.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import g1t1.features.logger.AppLogger;
import g1t1.features.logger.LogLevel;

/**
 * Manager for "Remember Me" functionality. Stores user credentials securely
 * (with basic obfuscation). Note: This is not cryptographically secure and
 * should not be used in production. For production use, implement proper
 * encryption or use OS-level credential storage.
 */
public class RememberMeManager {
    private static final String REMEMBER_FILE = ".remember";
    private static RememberMeManager instance;
    private final Gson gson;
    private RememberedCredentials credentials;

    private RememberMeManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadCredentials();
    }

    public static RememberMeManager getInstance() {
        if (instance == null) {
            instance = new RememberMeManager();
        }
        return instance;
    }

    /**
     * Load remembered credentials from file.
     */
    private void loadCredentials() {
        File rememberFile = new File(REMEMBER_FILE);

        if (!rememberFile.exists()) {
            this.credentials = null;
            return;
        }

        try (FileReader reader = new FileReader(rememberFile)) {
            RememberedCredentials stored = gson.fromJson(reader, RememberedCredentials.class);
            if (stored != null && stored.email != null && stored.password != null) {
                // Decode the obfuscated password
                this.credentials = new RememberedCredentials(stored.email, decodePassword(stored.password));
                AppLogger.logf("Remember me credentials loaded for user: %s", stored.email);
            } else {
                this.credentials = null;
            }
        } catch (Exception e) {
            AppLogger.logf(LogLevel.Warning, "Error loading remember me credentials: %s", e.getMessage());
            this.credentials = null;
        }
    }

    /**
     * Save credentials to remember.
     * 
     * @param email    User's email
     * @param password User's password
     */
    public void rememberCredentials(String email, String password) {
        try {
            // Obfuscate password with Base64 (NOT SECURE - just basic obfuscation)
            String encoded = encodePassword(password);
            RememberedCredentials toStore = new RememberedCredentials(email, encoded);

            try (FileWriter writer = new FileWriter(REMEMBER_FILE)) {
                gson.toJson(toStore, writer);
                this.credentials = new RememberedCredentials(email, password);
                AppLogger.logf("Remember me credentials saved for user: %s", email);
            }
        } catch (IOException e) {
            AppLogger.logf(LogLevel.Error, "Error saving remember me credentials: %s", e.getMessage());
        }
    }

    /**
     * Clear remembered credentials.
     */
    public void forgetCredentials() {
        File rememberFile = new File(REMEMBER_FILE);
        if (rememberFile.exists()) {
            boolean deleted = rememberFile.delete();
            if (deleted) {
                AppLogger.log("Remember me credentials cleared");
            } else {
                AppLogger.log(LogLevel.Warning, "Failed to delete remember me file");
            }
        }
        this.credentials = null;
    }

    /**
     * Check if credentials are remembered.
     */
    public boolean hasRememberedCredentials() {
        return credentials != null && credentials.email != null && credentials.password != null;
    }

    /**
     * Get remembered email.
     */
    public String getRememberedEmail() {
        return hasRememberedCredentials() ? credentials.email : "";
    }

    /**
     * Get remembered password.
     */
    public String getRememberedPassword() {
        return hasRememberedCredentials() ? credentials.password : "";
    }

    /**
     * Basic password obfuscation using Base64. NOTE: This is NOT secure encryption!
     * It's just basic obfuscation.
     */
    private String encodePassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    /**
     * Decode obfuscated password.
     */
    private String decodePassword(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Simple POJO for storing credentials.
     */
    private static class RememberedCredentials {
        String email;
        String password;

        RememberedCredentials(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
