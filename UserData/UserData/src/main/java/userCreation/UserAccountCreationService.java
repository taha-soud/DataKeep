package userCreation;


import exceptions.BadRequestException;
import exceptions.NotFoundException;
import iam.IUserService;
import iam.UserProfile;
import iam.UserType;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAccountCreationService {
    private final IUserService userService;
    private static final Logger logger = Logger.getLogger(UserAccountCreationService.class.getName());

    public UserAccountCreationService(IUserService userService) {
        this.userService = userService;
    }

    public void createNewUserAccount() {
        System.out.print("Enter a username for the new account: ");
        Scanner scanner = new Scanner(System.in);

        String newUsername = scanner.nextLine();

        try {
            // Check if the username already exists
            userService.getUser(newUsername);
            // If this line is reached, the user exists
            logger.log(Level.WARNING, "A user with username '" + newUsername + "' already exists.");
        } catch (NotFoundException e) {
            // Username does not exist, check if it was hard-deleted
            if (DeletedUsernamesTracker.isUsernameDeleted(newUsername)) {
                logger.log(Level.WARNING, "This username has been permanently deleted and cannot be reused.");
                return;
            }

            // Safe to proceed with creating a new user
            try {
                UserProfile newUser = new UserProfile();
                newUser.setUserName(newUsername);
                newUser.setEmail(newUsername + "email");
                newUser.setFirstName(newUsername + "first");
                newUser.setLastName(newUsername + "last");
                newUser.setPhoneNumber(newUsername + "phone");
                newUser.setPassword(newUsername + "pass");
                newUser.setRole(newUsername + "role");
                newUser.setDepartment(newUsername + "department");
                newUser.setOrganization(newUsername + "organization");
                newUser.setCountry(newUsername + "country");
                newUser.setCity(newUsername + "city");
                newUser.setStreet(newUsername + "street");
                newUser.setPostalCode(newUsername + "Postal");
                newUser.setBuilding(newUsername + "buliding");
                newUser.setUserType(UserType.NEW_USER);
                // Set other necessary fields for the new user

                // Additional validations can be added here as needed
                if (newUsername == null || newUsername.trim().isEmpty()) {
                    throw new BadRequestException("Username cannot be null or empty.");
                }

                userService.addUser(newUser);
                logger.info("New account created with username : " + newUsername);

            } catch (IllegalArgumentException e1) {
                logger.log(Level.SEVERE, "Error: " + e1.getMessage(), e1);
                System.err.println("Error: " + e1.getMessage());
            } catch (Exception e2) {
                logger.log(Level.SEVERE, "Unexpected error: " + e2.getMessage(), e2);
                System.err.println("Unexpected error: " + e2.getMessage());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred while checking for existing user: " + e.getMessage(), e);
            System.err.println("Error occurred while checking for existing user: " + e.getMessage());
        }
    }
}
