package deletion;

import activity.IUserActivityService;
import activity.UserActivity;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;
import iam.IUserService;
import payment.IPayment;
import payment.Transaction;
import posts.IPostService;
import posts.Post;
import userCreation.DeletedUsernamesTracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HardDeletionService implements UserDeletionService {
    private final IPostService postService;
    private final IPayment paymentService;
    private final IUserService userService;
    private final IUserActivityService userActivityService;
    private final DeletedUsernamesTracker deletedUsernamesTracker;

    private final ExecutorService executorService;
    private final Logger logger;

    public HardDeletionService(IPostService postService, IPayment paymentService, IUserService userService,
                               IUserActivityService userActivityService, DeletedUsernamesTracker instance) {
        this.postService = postService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.userActivityService = userActivityService;
        this.deletedUsernamesTracker = instance;

        // Create an ExecutorService with a fixed number of threads for parallel execution
        executorService = Executors.newFixedThreadPool(10);

        // Initialize the logger
        logger = Logger.getLogger(HardDeletionService.class.getName());
    }

    @Override
    public void executeDeletion(String userName) {
        AtomicBoolean deletionFailed = new AtomicBoolean(false); // Flag to track if any deletion task fails

        try {
            logger.info("Starting hard deletion for user: " + userName);

            // Check if the user exists before proceeding with deletion
            try {
                userService.getUser(userName);
            } catch (NotFoundException e) {
                // Handle the case where the user is not found
                logger.severe("User not found: " + e.getMessage());
                return; // Exit the method without further processing
            } catch (SystemBusyException | BadRequestException e) {
                // Handle other exceptions by throwing a RuntimeException
                logger.log(Level.SEVERE, "Error while checking user: " + userName, e);
                throw new RuntimeException(e);
            }

            // Submit tasks for parallel execution
            executorService.submit(() -> {
                // Delete Posts
                try {
                    List<Post> posts = new ArrayList<>(postService.getPosts(userName));
                    for (Iterator<Post> iterator = posts.iterator(); iterator.hasNext();) {
                        Post post = iterator.next();
                        postService.deletePost(userName, post.getId());
                        iterator.remove();
                    }
                } catch (NotFoundException e) {
                    logger.warning("Posts not found for user " + userName + ": " + e.getMessage());
                } catch (BadRequestException | SystemBusyException e) {
                    logger.log(Level.SEVERE, "Error deleting posts for user " + userName, e);
                    deletionFailed.set(true); // Set the flag to indicate deletion failure
                }
            });

            executorService.submit(() -> {
                // Delete Transactions
                try {
                    List<Transaction> transactions = new ArrayList<>(paymentService.getTransactions(userName));
                    for (Iterator<Transaction> iterator = transactions.iterator(); iterator.hasNext();) {
                        Transaction transaction = iterator.next();
                        paymentService.removeTransaction(userName, transaction.getId());
                        iterator.remove();
                    }
                } catch (NotFoundException e) {
                    logger.warning("Transactions not found for user " + userName + ": " + e.getMessage());
                } catch (BadRequestException | SystemBusyException e) {
                    logger.log(Level.SEVERE, "Error deleting transactions for user " + userName, e);
                    deletionFailed.set(true); // Set the flag to indicate deletion failure
                }
            });

            executorService.submit(() -> {
                // Delete User Activities
                try {
                    List<UserActivity> activities = new ArrayList<>(userActivityService.getUserActivity(userName));
                    for (Iterator<UserActivity> iterator = activities.iterator(); iterator.hasNext();) {
                        UserActivity activity = iterator.next();
                        userActivityService.removeUserActivity(userName, activity.getId());
                        iterator.remove();
                    }
                } catch (NotFoundException e) {
                    logger.warning("Activities not found for user " + userName + ": " + e.getMessage());
                } catch (BadRequestException | SystemBusyException e) {
                    logger.log(Level.SEVERE, "Error deleting activities for user " + userName, e);
                    deletionFailed.set(true); // Set the flag to indicate deletion failure
                }
            });

            executorService.submit(() -> {
                // Delete User Profile
                try {
                    userService.deleteUser(userName);
                    DeletedUsernamesTracker.markUsernameAsDeleted(userName); // Mark the username as deleted
                } catch (NotFoundException e) {
                    logger.warning("User profile not found for user " + userName + ": " + e.getMessage());
                    deletionFailed.set(true); // Set the flag to indicate deletion failure
                } catch (BadRequestException | SystemBusyException e) {
                    logger.log(Level.SEVERE, "Error deleting user profile for " + userName, e);
                    deletionFailed.set(true); // Set the flag to indicate deletion failure
                }
            });

            // Properly shut down the ExecutorService
            executorService.shutdown();
            try {
                // Wait for all tasks to complete or timeout after a specified time
                executorService.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "ExecutorService was interrupted", e);
                Thread.currentThread().interrupt(); // Restore interrupted status
            }

            // Mark the username as deleted after all tasks have completed
            if (deletionFailed.get()) {
                // Handle the case where deletion was not successful
                logger.severe("Deletion for user " + userName + " was not successful.");
                // You can perform additional actions or throw an exception as needed.
            } else {
                // Indicate that the deletion was successful
                logger.info("Deletion for user " + userName + " was successful.");
                DeletedUsernamesTracker.markUsernameAsDeleted(userName);
            }
        } catch (Exception ex) {
            // Handle any other exceptions that might occur within the method
            logger.log(Level.SEVERE, "An unexpected error occurred during user deletion: " + ex.getMessage(), ex);
        }
    }
}