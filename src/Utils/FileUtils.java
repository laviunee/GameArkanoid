package Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tiện ích đọc/ghi file
 */
public class FileUtils {

    public static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + filePath + " - " + e.getMessage());
            return null;
        }
    }

    public static void writeFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Lỗi ghi file: " + filePath + " - " + e.getMessage());
        }
    }

    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    // Đọc properties file
    public static java.util.Properties loadProperties(String filePath) {
        java.util.Properties props = new java.util.Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Lỗi load properties: " + filePath);
        }
        return props;
    }

    public static void saveProperties(java.util.Properties props, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            props.store(fos, null);
        } catch (IOException e) {
            System.err.println("Lỗi save properties: " + filePath);
        }
    }
}