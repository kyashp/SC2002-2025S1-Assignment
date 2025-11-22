package boundary;

import entity.domain.*;
import util.InputHelper;

/**
 * Main boundary controller for driving the Internship Placement Management System.
 * <p>
 * This class serves as the top-level console controller responsible for:
 * <ul>
 *     <li>Displaying the main entry menu</li>
 *     <li>Handling login and registration navigation</li>
 *     <li>Delegating to the correct user-specific UI module</li>
 * </ul>
 *
 * <p>
 * It follows the Dependency Inversion Principle (DIP):
 * The ConsoleUI depends on the abstract {@link UserInterface} (returned by {@link UIFactory}),
 * not concrete UI classes. This makes the system extensible and testable.
 * </p>
 */
public class ConsoleUI {

    /** UI responsible for authentication (login + registration). */
    private final AuthUI authUI;

    /**
     * Factory that maps a {@link User} to the correct UI implementation
     * (StudentUI, CompanyUI, StaffUI).
     */
    private final UIFactory uiFactory;

    /** Helper for validated input and formatted output. */
    private final InputHelper input;

    /**
     * Constructs a ConsoleUI instance.
     *
     * @param authUI the authentication handler for login/register workflows
     * @param uiFactory factory used to obtain the correct user-specific UI screen
     * @param input utility helper for validated user input
     */
    public ConsoleUI(AuthUI authUI, UIFactory uiFactory, InputHelper input) {
        this.authUI = authUI;
        this.uiFactory = uiFactory;
        this.input = input;
    }

    /**
     * Starts and runs the main system loop.
     * Continuously displays the main menu for:
     * <ul>
     *     <li>Login</li>
     *     <li>Registration for company representatives</li>
     *     <li>Application exit</li>
     * </ul>
     * This method blocks until the user chooses Exit (0).
     */
    public void start() {
        while (true) {
            input.printHeader("Internship Placement Management System");

            System.out.println("\nNote: For first-time Students and Career Center Staff, proceed to Login with the default password.");
            System.out.println("Note: For first-time Company Representatives, please Register for an account.\n");
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
                case 0 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("\n<<Invalid choice!>>");
            }
        }
    }

    /**
     * Launches the appropriate user-specific UI module after a successful login.
     * <p>
     * This method uses {@link UIFactory} to retrieve the correct UI based on the
     * user's role (Student, Company Representative, or Career Center Staff).
     *
     * @param user the authenticated user whose interface should be launched
     */
    private void launchUserInterface(User user) {
        try {
            // DIP in action: ConsoleUI does NOT depend on concrete UI types.
            UserInterface ui = uiFactory.getUI(user);
            ui.start();
        } 
        catch (IllegalArgumentException e) {
            System.out.println("System Error: " + e.getMessage());
        }
    }
}
