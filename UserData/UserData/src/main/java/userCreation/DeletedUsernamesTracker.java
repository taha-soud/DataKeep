package userCreation;

import java.util.HashSet;
import java.util.Set;

public class DeletedUsernamesTracker {
    private static DeletedUsernamesTracker instance = null;
    private static Set<String> deletedUsernames;

    private DeletedUsernamesTracker() {
        deletedUsernames = new HashSet<>();
    }

    public static DeletedUsernamesTracker getInstance() {
        if (instance == null) {
            instance = new DeletedUsernamesTracker();
        }
        return instance;
    }

    public static void markUsernameAsDeleted(String username) {
        deletedUsernames.add(username);
    }

    public static boolean isUsernameDeleted(String username) {
        return deletedUsernames.contains(username);
    }
}
