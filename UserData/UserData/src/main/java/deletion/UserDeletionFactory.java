package deletion;


import activity.IUserActivityService;
import iam.IUserService;
import payment.IPayment;
import posts.IPostService;
import userCreation.DeletedUsernamesTracker;

public class UserDeletionFactory {
    public static UserDeletionService getDeletionService(String type, IPostService postService, IPayment paymentService, IUserService userService, IUserActivityService userActivityService) {
        if ("hard".equalsIgnoreCase(type)) {
            return new HardDeletionService(postService, paymentService, userService, userActivityService, DeletedUsernamesTracker.getInstance());
        } else if ("soft".equalsIgnoreCase(type)) {
            return new SoftDeletionService(postService, paymentService, userService, userActivityService);
        } else {
            throw new IllegalArgumentException("Invalid deletion service type");
        }
    }
}
