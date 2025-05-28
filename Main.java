import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("No arguments provided.");
        }

        Set<String> actions = Set.of("add", "delete");

        if (actions.contains(args[1]) && args.length < 3) {
            System.out.println("Insufficient arguments.");
        }

    }
}