package export;

import payment.IPayment;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import activity.IUserActivityService;
import activity.UserActivity;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;
import iam.IUserService;
import iam.UserProfile;
import posts.IPostService;
import posts.Post;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportData implements IExportData {
    private static final Logger logger = Logger.getLogger(ExportData.class.getName());

    private final IUserService userService;
    private final IPostService postService;
    private final IPayment paymentService;
    private final IUserActivityService userActivityService;

    public ExportData(IUserService userService, IPostService postService,
                      IPayment paymentService, IUserActivityService userActivityService) {
        this.userService = userService;
        this.postService = postService;
        this.paymentService = paymentService;
        this.userActivityService = userActivityService;
    }

    @Override
    public Path exportAndZipUserData(String userId) throws BadRequestException, NotFoundException, SystemBusyException {
        logger.info("Exporting data for user: " + userId);
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID is required");
        }

        try {
            Path directory = Paths.get("C:\\Users\\ahmad\\OneDrive\\Desktop\\New folder\\Java-Project\\UserData\\UserData\\ZippedFile");
            Files.createDirectories(directory);
            UserProfile userProfile = userService.getUser(userId);
            if (userProfile == null) {
                throw new NotFoundException("User profile not found for ID: " + userId);
            }

            List<Post> userPosts = postService.getPosts(userId);
            List<UserActivity> userActivities = userActivityService.getUserActivity(userId);
            double userBalance = paymentService.getBalance(userId);

            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(formatUserProfile(userProfile));

            boolean hasPosts = userPosts != null && !userPosts.isEmpty();
            boolean hasActivities = userActivities != null && !userActivities.isEmpty();
            boolean isPremiumUser = userBalance > 0;

            if (hasPosts) {
                contentBuilder.append(formatUserPosts(userPosts));
            } else if (hasActivities) {
                contentBuilder.append(formatUserActivities(userActivities));
            }

            Path profilePdfPath = createPdf(contentBuilder.toString(), directory, "UserData");

            Path zipPath = directory.resolve(userId + "_data.zip");
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                addFileToZip(zos, profilePdfPath);

                if (isPremiumUser) {
                    String paymentContent = formatUserBalance(userId, userBalance);
                    Path paymentPdfPath = createPdf(paymentContent, directory, "UserPayments");
                    addFileToZip(zos, paymentPdfPath);
                    Files.deleteIfExists(paymentPdfPath);
                }
            }

            logger.info("Data exported successfully for user: " + userId);
            Files.deleteIfExists(profilePdfPath);

            return zipPath;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "System is currently unable to process the request due to: " + e.getMessage(), e);
            throw new SystemBusyException("System is currently unable to process the request due to: " + e.getMessage());
        }
    }


    private String formatUserProfile(UserProfile userProfile) {
        return String.format("UserProfile:\nName: %s %s\nEmail: %s\nPhone: %s\n\n",
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getEmail(),
                userProfile.getPhoneNumber());
    }

    private String formatUserPosts(List<Post> posts) {
        StringBuilder builder = new StringBuilder("UserPosts:\n");
        for (Post post : posts) {
            builder.append(String.format("Title: %s\nBody: %s\nDate: %s\n\n",
                    post.getTitle(),
                    post.getBody(),
                    post.getDate()));
        }
        return builder.toString();
    }

    private String formatUserActivities(List<UserActivity> activities) {
        StringBuilder builder = new StringBuilder("UserActivities:\n");
        for (UserActivity activity : activities) {
            builder.append(String.format("Type: %s\nDate: %s\n\n",
                    activity.getActivityType(),
                    activity.getActivityDate()));
        }
        return builder.toString();
    }

    private String formatUserBalance(String userId, double balance) {
        return String.format("UserBalance:\nUser: %s\nBalance: %.2f\n\n",
                userId, balance);
    }

    private Path createPdf(String content, Path directory, String fileName) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph(content));
        document.close();

        Path pdfPath = directory.resolve(fileName + ".pdf");
        Files.write(pdfPath, baos.toByteArray());

        return pdfPath;
    }

    private void addFileToZip(ZipOutputStream zos, Path filePath) throws Exception {
        ZipEntry zipEntry = new ZipEntry(filePath.getFileName().toString());
        zos.putNextEntry(zipEntry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }


}