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


public class SoftDeletionService implements UserDeletionService {
    private final IPostService postService;
    private final IPayment paymentService;
    private final IUserService userService;
    private final IUserActivityService userActivityService;

    private final ExecutorService executorService;
    private final Logger logger;

    public SoftDeletionService(IPostService postService, IPayment paymentService, IUserService userService,
                               IUserActivityService userActivityService) {
        this.postService = postService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.userActivityService = userActivityService;

        // Create an ExecutorService with a fixed number of threads for parallel execution
        int numThreads = Runtime.getRuntime().availableProcessors(); // Use the number of available processors
        executorService = Executors.newFixedThreadPool(numThreads);

        // Initialize the logger
        logger = Logger.getLogger(SoftDeletionService.class.getName());
    }

    @Override
    public void executeDeletion(String userName) {
        AtomicBoolean deletionFailed = new AtomicBoolean(false); // Flag to track if any deletion task fails

        logger.info("Starting soft deletion for user: " + userName);

        // Check if the user exists before proceeding with deletion
        try {
            try {
                userService.getUser(userName);
            } catch (SystemBusyException | BadRequestException e) {
                throw new RuntimeException(e);
            }
        } catch (NotFoundException e) {
            logger.severe("User not found: " + e.getMessage());
            return; // Exit the method without further processing
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

        // Shutdown the ExecutorService and wait for all tasks to complete
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(10,TimeUnit.SECONDS)) {
                logger.warning("Deletion tasks did not complete within 1 second.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Deletion was interrupted: " + e.getMessage(), e);
        }

        // Check if all tasks completed successfully
        if (!deletionFailed.get()) {
            // Indicate that the deletion was successful
            logger.info("Deletion for user " + userName + " was successful.");
        } else {
            // Handle the case where deletion was not successful
            logger.severe("Deletion for user " + userName + " was not successful.");
            // You can perform additional actions or throw an exception as needed.
        }
    }
}