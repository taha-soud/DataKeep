package deletion;


import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;

public interface UserDeletionService {
    void executeDeletion(String userName) throws BadRequestException, NotFoundException, SystemBusyException;
}



