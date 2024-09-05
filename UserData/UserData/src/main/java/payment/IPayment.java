package payment;

import exceptions.BadRequestException;
import exceptions.NotFoundException;
import exceptions.SystemBusyException;

import java.util.List;

public interface IPayment {
    void pay(Transaction transaction);
    double getBalance(String userName);
    void removeTransaction(String userName, String id) throws SystemBusyException, BadRequestException, NotFoundException;
    List<Transaction> getTransactions(String userName) throws SystemBusyException, BadRequestException, NotFoundException;
}
