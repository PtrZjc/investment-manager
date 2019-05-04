package pl.zajacp.investmentmanager.registration;

import pl.zajacp.investmentmanager.user.User;
import pl.zajacp.investmentmanager.validation.EmailExistsException;

public interface IUserService {
    User registerNewUserAccount(AccountDto accountDto)
      throws EmailExistsException;
}