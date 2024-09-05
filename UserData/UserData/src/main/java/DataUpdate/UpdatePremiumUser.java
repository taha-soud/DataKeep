package DataUpdate;


import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;
import iam.IUserService;
import iam.UserProfile;
import iam.UserType;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdatePremiumUser {
    private final UserProfile userProfile;
    private final IUserService userService;
    private final Logger logger;

    public UpdatePremiumUser(UserProfile userProfile, IUserService userService) {
        this.userProfile = userProfile;
        this.userService = userService;
        this.logger = Logger.getLogger(UpdatePremiumUser.class.getName());
    }

    public void updatePremiumInformation() {
        // Check if the user is a premium user
        Scanner scanner = new Scanner(System.in);

        if (userProfile.getUserType() == UserType.PREMIUM_USER) {
            logger.log(Level.INFO, "Updating premium user information for user: {0}", userProfile.getUserName());

            System.out.println("Enter new phone number (or press Enter to skip): ");
            String newPhoneNumber = scanner.nextLine();
            if (!newPhoneNumber.isEmpty()) {
                userProfile.setPhoneNumber(newPhoneNumber);
                logger.log(Level.INFO, "Phone number updated to: {0}", newPhoneNumber);
            }

            System.out.println("Enter new email (or press Enter to skip): ");
            String newEmail = scanner.nextLine();
            if (!newEmail.isEmpty()) {
                userProfile.setEmail(newEmail);
                logger.log(Level.INFO, "Email updated to: {0}", newEmail);
            }

            System.out.println("Enter new department (or press Enter to skip): ");
            String newDepartment = scanner.nextLine();
            if (!newDepartment.isEmpty()) {
                userProfile.setDepartment(newDepartment);
                logger.log(Level.INFO, "Department updated to: {0}", newDepartment);
            }

            System.out.println("Enter new organization (or press Enter to skip): ");
            String newOrganization = scanner.nextLine();
            if (!newOrganization.isEmpty()) {
                userProfile.setOrganization(newOrganization);
                logger.log(Level.INFO, "Organization updated to: {0}", newOrganization);
            }

            System.out.println("Enter new city (or press Enter to skip): ");
            String newCity = scanner.nextLine();
            if (!newCity.isEmpty()) {
                userProfile.setCity(newCity);
                logger.log(Level.INFO, "City updated to: {0}", newCity);
            }

            System.out.println("Enter new street (or press Enter to skip): ");
            String newStreet = scanner.nextLine();
            if (!newStreet.isEmpty()) {
                userProfile.setStreet(newStreet);
                logger.log(Level.INFO, "Street updated to: {0}", newStreet);
            }

            System.out.println("Enter new postal code (or press Enter to skip): ");
            String newPostalCode = scanner.nextLine();
            if (!newPostalCode.isEmpty()) {
                userProfile.setPostalCode(newPostalCode);
                logger.log(Level.INFO, "Postal code updated to: {0}", newPostalCode);
            }

            System.out.println("Enter new first name (or press Enter to skip): ");
            String newFirstName = scanner.nextLine();
            if (!newFirstName.isEmpty()) {
                userProfile.setFirstName(newFirstName);
                logger.log(Level.INFO, "First name updated to: {0}", newFirstName);
            }

            System.out.println("Enter new last name (or press Enter to skip): ");
            String newLastName = scanner.nextLine();
            if (!newLastName.isEmpty()) {
                userProfile.setLastName(newLastName);
                logger.log(Level.INFO, "Last name updated to: {0}", newLastName);
            }

            System.out.println("Enter new role (or press Enter to skip): ");
            String newRole = scanner.nextLine();
            if (!newRole.isEmpty()) {
                userProfile.setRole(newRole);
                logger.log(Level.INFO, "Role updated to: {0}", newRole);
            }

            System.out.println("Enter new country (or press Enter to skip): ");
            String newCountry = scanner.nextLine();
            if (!newCountry.isEmpty()) {
                userProfile.setCountry(newCountry);
                logger.log(Level.INFO, "Country updated to: {0}", newCountry);
            }

            // Attempt to update user information
            try {
                userService.updateUser(userProfile);
                logger.log(Level.INFO, "User information updated successfully for user: {0}", userProfile.getUserName());
            } catch (NotFoundException e) {
                logger.log(Level.SEVERE, "User not found while updating user information: {0}", e.getMessage());
                throw new RuntimeException(e);
            } catch (SystemBusyException e) {
                logger.log(Level.SEVERE, "System is busy while updating user information: {0}", e.getMessage());
                throw new RuntimeException(e);
            } catch (BadRequestException e) {
                logger.log(Level.SEVERE, "Bad request while updating user information: {0}", e.getMessage());
                throw new RuntimeException(e);
            }

        }
    }
}