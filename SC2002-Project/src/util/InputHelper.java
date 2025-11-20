package util;

import java.util.Scanner;

/**
 * <<Helper>> InputHelper
 * Handles console input/output to prevent code duplication.
 */
public class InputHelper {
    private final Scanner sc;

    public InputHelper(Scanner sc) {
        this.sc = sc;
    }

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

    public String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
    
    public void printHeader(String title) {
        String border = "=".repeat(40);
        System.out.println("\n" + border);
        System.out.println(" " + title);
        System.out.println(border);
    }
}