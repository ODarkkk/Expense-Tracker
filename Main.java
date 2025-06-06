import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.Month;


public class Main {
    public static final String FILE = "expenses.csv";
    public static final String[] REQUIREDHEADERS = { "ID", "Date", "Description", "Amount" };
    private static final Set<String> ACTIONS = Set.of("add", "delete", "update", "list", "summary");
    private static Map<Integer, Item> items = new HashMap<>();

    public static void main(String[] args) {
        FileVerifier();
        LoadExpenses();
        ProcessCommand(args);
    }

    public static void ProcessCommand(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("No command provided.");
            return;
        }

        String action = args[0].toLowerCase(); // Case-insensitive comparison

        // Validate action
        if (!ACTIONS.contains(action)) {
            System.err.println("Invalid action.");
            return;
        }

        try {
            // Handle each action with appropriate argument validation
            switch (action) {
                case "add":
                    if (args.length < 3) {
                        System.out.println("Insufficient arguments for add command.");
                        return;
                    }
                    ValidateNoIdArgument(args);
                    AddExpense(ReceiveArguments(args));
                    break;

                case "delete":
                case "update":
                    if (args.length < 3) {
                        System.out.println("Insufficient arguments for " + action + " command.");
                        return;
                    }
                    if (action.equals("delete")) {
                        DeleteExpense(ReceiveArguments(args));
                    } else {
                        UpdateExpense(ReceiveArguments(args));
                    }
                    break;

                case "list":
                case "summary":
                    // These commands don't require additional arguments beyond the action
                    if (action.equals("list")) {
                        ListExpenses(ReceiveArguments(args));
                    } else {
                        ListSummary(ReceiveArguments(args));
                    }
                    break;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private static void ValidateNoIdArgument(String[] args) throws IllegalArgumentException {
        for (String arg : args) {
            if (arg.contains("--id")) {
                throw new IllegalArgumentException("Invalid argument: --id is not allowed for add command");
            }
        }
    }

    private static void ListExpenses(Map<String, String> arguments) {
        for (String header : REQUIREDHEADERS) {
            System.out.print(header);
        }
        System.out.println();
        for (Item item : items.values()) {
            System.out.println(item.toString());
        }
    }

    private static void ListSummary(Map<String, String> arguments) {
        if (arguments.isEmpty()) {
            ListAllSummary();
        } else {
            ListFilteredSummary(arguments);
        }
    }

    private static void ListAllSummary() {
        int expenses = 0;
        for (Item item : items.values()) {
            expenses += item.GetAmount();
        }
        System.out.println("Total expenses: $" + expenses);
    }

    private static void ListFilteredSummary(Map<String, String> arguments) {
        int expenses = 0;
        for (Item item : items.values()) {
            if (item.GetDate().equals(arguments.get("month"))) {
                expenses += item.GetAmount();
            }
        }
        System.out.println(
                "Total expenses for month " + Month.of(Integer.parseInt(arguments.get("month"))) + ": $" + expenses);
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
        if (!arguments.containsKey("description") || !arguments.containsKey("amount")) {
            throw new IllegalArgumentException("Missing required arguments: description and amount are required");
        }
        try {
            // Now you can access the values like this:
            String description = arguments.get("description");
            String date = LocalDate.now().toString();
            int amount = Integer.parseInt(arguments.get("amount")); // Convert to int
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            // Implement your expense adding logic here
            int maxId = 0;
            for (Integer id : items.keySet()) {
                maxId = Math.max(maxId, id);
            }
            // Generate a new ID
            int id = maxId + 1;
            Item item = new Item(id, date, description, amount);
            items.put(id, item);
            WriteFile(item.toString());
            // System.out.println("Expense added successfully.");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());

        }
    }

    private static void DeleteExpense(Map<String, String> arguments) {
        String id = arguments.get("id");
        if (GetId(Integer.parseInt(id)) != null) {
            int idInt = Integer.parseInt(id);
            items.remove(idInt);
            WriteFile();
        }
    }

    private static void UpdateExpense(Map<String, String> arguments) {
        String id = arguments.get("id");
        String description = arguments.get("description");
        int amount = Integer.parseInt(arguments.get("amount")); // Convert to int
        if (GetId(Integer.parseInt(id)) != null) {
            int idInt = Integer.parseInt(id);
            items.get(idInt).SetDescription(description);
            items.get(idInt).SetAmount(amount);
            WriteFile();
        }
    }

    // Example output format:
    // ID Date Description Amount
    // 1 12/12/2022 Food 100

    private static Item GetId(int id) {
        Item item = items.get(id);
        if (item == null) {
            System.err.println("No item found with ID " + id);
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

                // In LoadExpenses(), change the split pattern:
                String[] values = line.split("\t", -1); // Use tab instead of comma

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
            // System.out.println(items.getClass());

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
                for (String header : REQUIREDHEADERS) {
                    WriteFile(header);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }

    public static void WriteFile(String line) {
        try (FileWriter writer = new FileWriter(FILE, true)) { // 'true' enables appending
            writer.write(line + "\t");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void WriteFile() {
        try (FileWriter writer = new FileWriter(FILE)) {
            // Write Header
            for (String header : REQUIREDHEADERS) {
                writer.write(header + "\t");
            }

            // Write all items
            for (Item item : items.values()) {
                writer.write(item.GetId() + "\t" +
                        item.GetDate() + "\t" +
                        item.GetDescription() + "\t" +
                        item.GetAmount() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}