package export;
import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;

import java.nio.file.Path;

public interface IExportData {
    Path exportAndZipUserData(String userId) throws BadRequestException, NotFoundException, SystemBusyException;
}