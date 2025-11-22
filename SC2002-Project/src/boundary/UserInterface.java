package boundary;

/**
 * <<Interface>> UserInterface
 * Defines the contract for all role-specific UIs.
 * This allows for polymorphism in the main ConsoleUI and adheres to Open-Closed Principle.
 */

public interface UserInterface {
	void start();
}
