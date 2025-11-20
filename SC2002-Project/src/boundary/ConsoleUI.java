package boundary;

//import java.util.HashMap;
//import java.util.Map;

import entity.domain.*;
import util.InputHelper;


public class ConsoleUI {
    private final AuthUI authUI;
    private final UIFactory uiFactory;
    private final InputHelper input;

    public ConsoleUI(AuthUI authUI, UIFactory uiFactory, InputHelper input) {
        this.authUI = authUI;
        this.uiFactory = uiFactory;
        this.input = input;
    }

    public void start() {
        while (true) {
            input.printHeader("Internship Placement Management System");
            System.out.println("\nNote: For first-time Students and Career Center Staff proceed to Login with the default password.\nNote: For first-time Company Representatives please Register for an account.\n");
            System.out.println("1) Login");
            System.out.println("2) Register (Company Representative)");
            System.out.println("0) Exit");

            int choice = input.readInt("Choice: ");
            
            switch (choice) {
            case 1 -> {
                User user = authUI.handleLogin();
                if (user != null) {
                    launchUserInterface(user);
                }
            } 
            case 2 -> authUI.handleRegistration();
            case 0 -> {System.out.println("Goodbye!"); return;}
            default -> System.out.println("\n<<Invalid choice!>>");
        }
        }
    }

    private void launchUserInterface(User user) {
        try {
            // Dependency Injection Principle - UserInterface acts as an abstraction layer between higher level ConsoleUI and lower level UI modules.
            UserInterface ui = uiFactory.getUI(user);
            ui.start();
        } catch (IllegalArgumentException e) {
            System.out.println("System Error: " + e.getMessage());
        }
    }
}