package com.bx.ultimateDonutSmp.managers;

import com.bx.ultimateDonutSmp.UltimateDonutSmp;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

public class UpdateManager {

    private static final String VERSION_URL = "https://raw.githubusercontent.com/BeestoXd/UltimateDonutSMP/main/version.txt";

    private final UltimateDonutSmp plugin;
    private String latestVersion = null;
    private boolean updateAvailable = false;

    public UpdateManager(UltimateDonutSmp plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks for updates asynchronously from the raw GitHub version file.
     */
    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = URI.create(VERSION_URL).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String version = reader.readLine();
                    if (version != null) {
                        version = version.trim();
                        if (!version.isEmpty()) {
                            this.latestVersion = version;
                            String currentVersion = plugin.getDescription().getVersion();
                            this.updateAvailable = isNewerVersion(currentVersion, latestVersion);

                            if (this.updateAvailable) {
                                plugin.getLogger().warning("A new update is available: " + latestVersion + " (Current version: " + currentVersion + ")");
                                plugin.getLogger().warning("Please download the latest version from: https://github.com/BeestoXd/UltimateDonutSMP");
                            } else {
                                plugin.getLogger().info("You are running the latest version of UltimateDonutSmp (" + currentVersion + ").");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private String cleanVersion(String version) {
        if (version == null) {
            return "";
        }
        version = version.trim();
        if (version.toLowerCase().startsWith("v")) {
            version = version.substring(1).trim();
        }
        return version;
    }

    private boolean isNewerVersion(String current, String latest) {
        if (current == null || latest == null) {
            return false;
        }
        String cleanCurrent = cleanVersion(current);
        String cleanLatest = cleanVersion(latest);
        if (cleanCurrent.equalsIgnoreCase(cleanLatest)) {
            return false;
        }

        // Split by standard delimiters and compare segments numerically
        String[] currentParts = cleanCurrent.split("[-._]");
        String[] latestParts = cleanLatest.split("[-._]");
        int length = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < length; i++) {
            String currentPart = i < currentParts.length ? currentParts[i] : "0";
            String latestPart = i < latestParts.length ? latestParts[i] : "0";

            try {
                int currentNum = Integer.parseInt(currentPart);
                int latestNum = Integer.parseInt(latestPart);
                if (latestNum > currentNum) {
                    return true;
                } else if (currentNum > latestNum) {
                    return false;
                }
            } catch (NumberFormatException e) {
                // Fallback to lexicographical comparison
                int comp = latestPart.compareToIgnoreCase(currentPart);
                if (comp > 0) {
                    return true;
                } else if (comp < 0) {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
