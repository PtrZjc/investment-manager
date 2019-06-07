package pl.zajacp.investmentmanager.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.user.validation.EmailExistsException;
import pl.zajacp.investmentmanager.user.validation.LoginExistsException;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.userRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerNewUserAccount(UserDto userDto)
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
        userRepository.save(user);
    }

    private boolean emailExist(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    private boolean loginExists(String login) {
        User user = userRepository.findByLogin(login);
        return user != null;
    }

    public User getLoggedUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByLogin(principal.getUsername());
    }
}