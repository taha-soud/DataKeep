package data;
import DataUpdate.UpdatePremiumUser;
import com.google.api.services.drive.Drive;
import deletion.UserDeletionFactory;
import deletion.UserDeletionService;
import export.DriveService;
import export.ExportData;
import export.IExportData;
import payment.IPayment;
import payment.PaymentService;
import posts.IPostService;
import posts.Post;
import posts.PostService;
import activity.IUserActivityService;
import activity.UserActivity;
import activity.UserActivityService;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;
import exceptions.Util;
import iam.IUserService;
import iam.UserProfile;
import iam.UserService;
import iam.UserType;
import payment.Transaction;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Scanner;
import java.util.logging.Logger;
public class Application {
    private static final Logger logger = Logger.getLogger(Application.class.getName());
    private static final IUserActivityService userActivityService = new UserActivityService();
    private static final IPayment paymentService = new PaymentService();
    private static final IUserService userService = new UserService();
    private static final IPostService postService = new PostService();

    private static String loginUserName;
    public static void main(String[] args) throws GeneralSecurityException, IOException {

        generateRandomData();
        Instant start = Instant.now();
        System.out.println("Application Started: " + start);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        System.out.println("Note: You can use any of the following usernames: user0, user1, user2, user3, .... user99");
        String userName = scanner.nextLine();
        setLoginUserName(userName);

        //TODO Your application starts here. Do not Change the existing code



        IExportData exportData = new ExportData(userService, postService, paymentService, userActivityService);
        UserDeletionService hardDeletionService = UserDeletionFactory.getDeletionService("hard", postService, paymentService, userService, userActivityService);
        UserDeletionService softDeletionService = UserDeletionFactory.getDeletionService("soft", postService, paymentService, userService, userActivityService);
        UpdatePremiumUser updatePremiumUser = null;
        try {
            updatePremiumUser = new UpdatePremiumUser(userService.getUser(loginUserName), userService);
        } catch (NotFoundException | SystemBusyException | BadRequestException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Select an option:");
        System.out.println("1. Export user data");
        System.out.println("2. Delete user data");
        System.out.println("3. Update Profile (premium)");

        System.out.print("Enter your choice (1-3): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                String zipFile;
                try {
                    zipFile = String.valueOf(exportData.exportAndZipUserData(loginUserName));

                } catch (BadRequestException e) {
                    logger.warning("Invalid request: " + e.getMessage());
                    e.printStackTrace();
                    break;
                } catch (NotFoundException e) {
                    logger.warning("Data not found: " + e.getMessage());
                    e.printStackTrace();
                    break;
                } catch (SystemBusyException e) {
                    logger.warning("System is currently busy: " + e.getMessage());
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    logger.warning("An unexpected error occurred: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }

                System.out.println("Select the next step:");
                System.out.println("1. Download zip file");
                System.out.println("2. Upload to Google Drive");
                System.out.print("Enter your choice (1-2): ");
                int nextStepChoice = scanner.nextInt();
                scanner.nextLine();

                if (nextStepChoice == 1) {
                    logger.info("User chose to download the zip file.");
                    logger.info("The zip file has been exported to: " + zipFile);
                    System.out.println("You can download the zip file from: " + zipFile);
                } else if (nextStepChoice == 2) {
                    String zipFilePath = null;
                    String zipFileName = loginUserName ;
                    String folderId = "1mli_6xXQIcfkzU-6tJXkqbVx2z9Stadk";
                    Drive service = DriveService.initializeDriveService();
                    try {
                        zipFilePath = String.valueOf(exportData.exportAndZipUserData(loginUserName));
                        String fileId = DriveService.uploadZipFile(service, zipFilePath, zipFileName, folderId);
                        String fileLink = "https://drive.google.com/uc?id=" + fileId;
                        logger.info("File uploaded! Direct Download Link: " + fileLink);
                    } catch (BadRequestException e) {
                        logger.warning("Invalid request: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } catch (NotFoundException e) {
                        logger.warning("Data not found: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } catch (SystemBusyException e) {
                        logger.warning("System is currently busy: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        logger.warning("An unexpected error occurred during the data export process: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else {
                    logger.warning("Invalid option. Please choose 1 or 2.");
                }
                break;
            case 2:
                System.out.println("Choose the delete type:");
                System.out.println("1. Hard delete");
                System.out.println("2. Soft delete");
                System.out.print("Enter your choice (1-2): ");
                int deletionChoice = scanner.nextInt();
                scanner.nextLine();

                if (deletionChoice == 1) {

                    try {
                        hardDeletionService.executeDeletion(loginUserName);
                    } catch (BadRequestException e) {
                        throw new RuntimeException(e);
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (SystemBusyException e) {
                        throw new RuntimeException(e);
                    }

                }else if (deletionChoice == 2) {
                    try {
                        softDeletionService.executeDeletion(loginUserName);
                    } catch (BadRequestException e) {
                        throw new RuntimeException(e);
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (SystemBusyException e) {
                        throw new RuntimeException(e);
                    }
                }

                break;
            case 3:
                updatePremiumUser.updatePremiumInformation();
                break;
            default:
                System.out.println("Invalid option. Please choose 1, 2, or 3.");
                break;
        }

        //TODO Your application ends here. Do not Change the existing code
        Instant end = Instant.now();
        System.out.println("Application Ended: " + end);
    }


    private static void generateRandomData() {
        Util.setSkipValidation(true);
        for (int i = 0; i < 100; i++) {
            generateUser(i);
            generatePost(i);
            generatePayment(i);
            generateActivity(i);
        }
        System.out.println("Data Generation Completed");
        Util.setSkipValidation(false);
    }

    private static void generateActivity(int i) {
        for (int j = 0; j < 100; j++) {
            userActivityService.addUserActivity(new UserActivity("user" + i, "activity" + i + "." + j, Instant.now().toString()));
        }
    }

    private static void generatePayment(int i) {
        for (int j = 0; j < 100; j++) {
            try {
                if (userService.getUser("user" + i).getUserType() == UserType.PREMIUM_USER) {
                    paymentService.pay(new Transaction("user" + i, i * j, "description" + i + "." + j));
                }
            } catch (Exception e) {
                System.err.println("Error while generating post for user" + i);
            }
        }
    }

    private static void generatePost(int i) {
        for (int j = 0; j < 100; j++) {
            postService.addPost(new Post("title" + i + "." + j, "body" + i + "." + j, "user" + i, Instant.now().toString()));


        }
    }

    private static void generateUser(int i) {
        UserProfile user = new UserProfile();
        user.setUserName("user" + i);
        user.setFirstName("first" + i);
        user.setLastName("last" + i);
        user.setPhoneNumber("phone" + i);
        user.setEmail("email" + i);
        user.setPassword("pass" + i);
        user.setRole("role" + i);
        user.setDepartment("department" + i);
        user.setOrganization("organization" + i);
        user.setCountry("country" + i);
        user.setCity("city" + i);
        user.setStreet("street" + i);
        user.setPostalCode("postal" + i);
        user.setBuilding("building" + i);
        user.setUserType(getRandomUserType(i));
        userService.addUser(user);
    }

    private static UserType getRandomUserType(int i) {
        if (i > 0 && i < 3) {
            return UserType.NEW_USER;
        } else if (i > 3 && i < 7) {
            return UserType.REGULAR_USER;
        } else {
            return UserType.PREMIUM_USER;
        }
    }



    public static String getLoginUserName() {
        return loginUserName;
    }

    private static void setLoginUserName(String loginUserName) {
        Application.loginUserName = loginUserName;
    }
}
