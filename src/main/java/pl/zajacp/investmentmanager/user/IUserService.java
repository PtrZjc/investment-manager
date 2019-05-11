package pl.zajacp.investmentmanager.user;

import pl.zajacp.investmentmanager.user.registration.validation.EmailExistsException;
import pl.zajacp.investmentmanager.user.registration.validation.LoginExistsException;

public interface IUserService {
    User registerNewUserAccount(UserDto userDto)
      throws LoginExistsException, EmailExistsException;
}