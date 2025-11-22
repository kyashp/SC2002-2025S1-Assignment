package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * <<Helper>> InputHelper
 * Handles console input/output to prevent code duplication.
 */
public class InputHelper {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Scanner sc;

    /**
     * @param sc scanner used for reading console input
     */
    public InputHelper(Scanner sc) {
        this.sc = sc;
    }

    /**
     * Reads and parses an integer, re-prompting until valid.
     *
     * @param prompt text shown before reading
     * @return parsed integer
     */
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    /**
     * Reads a line of input after displaying the prompt.
     *
     * @param prompt text shown before reading
     * @return trimmed string
     */
    public String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    /**
     * Reads a date in DD/MM/YYYY format, ensuring it is on/after the provided minimum when specified.
     *
     * @param label display label for the prompt
     * @param minDate minimum acceptable date (inclusive), nullable
     * @return parsed LocalDate
     */
    public LocalDate readDateOnOrAfter(String label, LocalDate minDate) {
        while (true) {
            System.out.print(label + " (DD/MM/YYYY): ");
            String input = sc.nextLine().trim();
            try {
                LocalDate parsed = LocalDate.parse(input, DATE_FORMAT);
                if (minDate != null && parsed.isBefore(minDate)) {
                    System.out.printf("Date must be on or after %s.%n", minDate.format(DATE_FORMAT));
                    continue;
                }
                return parsed;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Please use DD/MM/YYYY.");
            }
        }
    }
    
    /**
     * Prints a formatted section header.
     *
     * @param title header text
     */
    public void printHeader(String title) {
        String border = "=".repeat(40);
        System.out.println("\n" + border);
        System.out.println(" " + title);
        System.out.println(border);
    }
}
