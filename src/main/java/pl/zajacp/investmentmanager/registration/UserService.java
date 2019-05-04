package pl.zajacp.investmentmanager.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.user.User;
import pl.zajacp.investmentmanager.user.UserRepository;
import pl.zajacp.investmentmanager.validation.exceptions.EmailExistsException;
import pl.zajacp.investmentmanager.validation.exceptions.LoginExistsException;

@Service
public class UserService implements IUserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.userRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public User registerNewUserAccount(UserDto userDto)
            throws LoginExistsException, EmailExistsException {

        if (loginExists(userDto.getLogin())){
            throw new LoginExistsException("Login already used");
        }else if(emailExist(userDto.getEmail())){
            throw new EmailExistsException("Email already used");
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setLogin(userDto.getLogin());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        return userRepository.save(user);

    }

    private boolean emailExist(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return true;
        }
        return false;
    }

    private boolean loginExists(String login) {
        User user = userRepository.findByLogin(login);
        if (user != null) {
            return true;
        }
        return false;
    }
}