package iam;

import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;

public interface IUserService {
    void addUser(UserProfile user);
    void updateUser(UserProfile user) throws NotFoundException, SystemBusyException, BadRequestException;
    void deleteUser(String userName) throws NotFoundException, SystemBusyException, BadRequestException;
    UserProfile getUser(String userName) throws NotFoundException, SystemBusyException, BadRequestException;
}
