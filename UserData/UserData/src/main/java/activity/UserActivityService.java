package activity;

import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;
import exceptions.Util;

import java.util.*;

public class UserActivityService implements IUserActivityService {
    private static final Map<String, List<UserActivity>> userActivityMap = new HashMap<>();
    @Override
    public void addUserActivity(UserActivity userActivity) {
        userActivityMap.computeIfAbsent(userActivity.getUserId(), key -> new ArrayList<>()).add(userActivity);
    }

    @Override
    public List<UserActivity> getUserActivity(String userId) throws SystemBusyException, BadRequestException, NotFoundException {
        Util.validateUserName(userId);
        if (!userActivityMap.containsKey(userId)) {
            throw new NotFoundException("User does not exist");
        }
        return userActivityMap.get(userId);
    }

    @Override
    public void removeUserActivity(String userId, String id) throws SystemBusyException, BadRequestException, NotFoundException {
        Util.validateUserName(userId);
        if (!userActivityMap.containsKey(userId)) {
            throw new NotFoundException("User does not exist");
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Iterator<UserActivity> iterator = userActivityMap.get(userId).iterator();
        while (iterator.hasNext()) {
            UserActivity activity = iterator.next();
            if (activity.getId().equals(id)) {
                iterator.remove();
            }
        }
        userActivityMap.put(userId, userActivityMap.get(userId));
    }
}
