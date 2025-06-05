import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.nio.file.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;

public class Main {
    public static final String FILE = "expenses.csv";
    public static final String[] requiredHeaders = { "ID", "Date", "Description", "Amount" };
    private static Map<Integer, Item> items = new HashMap<>();

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
        String date = LocalDate.now().toString();
        int amount = Integer.parseInt(arguments.get("amount")); // Convert to int

        // Implement your expense adding logic here
        int maxId = 0;
        for (Integer id : items.keySet()) {
            maxId = Math.max(maxId, id);
        }
        // Generate a new ID (assuming you have a way to generate unique IDs
        int id = maxId + 1;
        Item item = new Item(id, date, description, amount);
        items.put(id, item);
        WriteFile(item.toString());
        System.out.println("Expense added successfully.");
    }

    // Example output format:
    // ID Date Description Amount
    // 1 12/12/2022 Food 100

    private static Item GetId(int id) {
        Item item = items.get(id);
        if (item == null) {
            System.out.println("No item found with ID " + id);
            return null;
        }
        return item;
    }

    private static void LoadExpenses() {
        items.clear(); // Clear previous data if reloading

        Path filePath = Paths.get(FILE);

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue; // Skip empty lines

                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Skip malformed lines (missing columns)
                if (values.length < 4) {
                    System.err.println("Skipping malformed line (expected 4 columns): " + line);
                    continue;
                }

                try {
                    int id = Integer.parseInt(values[0].trim());
                    String date = values[1].trim();
                    String description = values[2].trim().replaceAll("^\"|\"$", "");
                    int amount = Integer.parseInt(values[3].trim());

                    Item item = new Item(id, date, description, amount);
                    items.put(id, item);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line (invalid number format): " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
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
                System.err.println("Error creating file: " + e.getMessage());
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                // Empty file
                WriteFile(requiredHeaders.toString());
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }

    public static void WriteFile(String line) {
        try (FileWriter writer = new FileWriter(FILE, true)) { // 'true' enables appending
            writer.write(line + "\n");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}
