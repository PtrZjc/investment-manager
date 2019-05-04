package pl.zajacp.investmentmanager.registration;

import pl.zajacp.investmentmanager.user.User;
import pl.zajacp.investmentmanager.validation.exceptions.EmailExistsException;
import pl.zajacp.investmentmanager.validation.exceptions.LoginExistsException;

public interface IUserService {
    User registerNewUserAccount(UserDto userDto)
      throws LoginExistsException, EmailExistsException;
}