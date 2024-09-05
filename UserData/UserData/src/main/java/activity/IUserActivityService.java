package activity;

import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;

import java.util.List;

public interface IUserActivityService {

    void addUserActivity(UserActivity userActivity);

    List<UserActivity> getUserActivity(String userId) throws SystemBusyException, BadRequestException, NotFoundException;

    void removeUserActivity(String userId, String id) throws SystemBusyException, BadRequestException, NotFoundException;
}
