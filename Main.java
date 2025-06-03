import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.nio.file.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

public class Main {
    public static final String FILE = "expenses.csv";
    public static final String[] requiredHeaders = { "ID", "Date", "Description", "Amount" };

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments provided.");
        }
        FileVerifier();
        Set<String> actions = Set.of("add", "delete");

        if (actions.contains(args[1]) && args.length < 3) {
            System.out.println("Insufficient arguments.");
        }
        if (args[0].equals("add") && args.length >= 3) {
            for (String arg : args) {
                if (arg.contains("--id")) {
                    throw new IllegalArgumentException("Invalid argument");
                }
                if (arg.contains("--description")) {
                    throw new IllegalArgumentException("Invalid argument");
                }
            }
            AddExpense(ReceiveArguments(args));
        }
    }

    private static Map<String, String> ReceiveArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        String currentKey = null;
        StringBuilder currentValue = new StringBuilder();

        for (String arg : args) {
            if (arg.startsWith("--")) {
                // Save the previous key-value pair
                if (currentKey != null) {
                    arguments.put(currentKey, currentValue.toString().trim());
                    currentValue.setLength(0); // Reset
                }
                currentKey = arg.substring(2); // Remove "--"
            } else {
                if (currentValue.length() > 0) {
                    currentValue.append(" ");
                }
                currentValue.append(arg);
            }
        }
        // Add the last key-value pair
        if (currentKey != null) {
            arguments.put(currentKey, currentValue.toString().trim());
        }
        return arguments;
    }

    private static void AddExpense(Map<String, String> arguments) {
        // Now you can access the values like this:
        String description = arguments.get("description");
        String amount = arguments.get("amount");

        // Implement your expense adding logic here
        System.out.println("Description: " + description);
        System.out.println("Amount: " + amount);

        // Example output format:
        // ID Date Description Amount
        // 1 12/12/2022 Food 100
    }

    public static void FileVerifier() {
        Path path = Paths.get(FILE);
        if (!Files.exists(path)) {
            try {
                System.out.println("File not found. Creating a new file.");
                // Create a new file (if it doesn't exist
                Files.createFile(path);
                FileVerifier();
            } catch (IOException e) {
                System.out.println("Error creating file: " + e.getMessage());
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                // Empty file
                WriteFile(requiredHeaders);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

    }

    public static void WriteFile(String[] string) {
        try (FileWriter writer = new FileWriter(FILE)) {
            for (String str : string) {
                writer.append(str);
            }
            writer.append("\n");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

}
