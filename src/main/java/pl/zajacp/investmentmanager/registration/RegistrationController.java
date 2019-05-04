package pl.zajacp.investmentmanager.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import pl.zajacp.investmentmanager.user.User;
import pl.zajacp.investmentmanager.validation.EmailExistsException;

import javax.validation.Valid;

@Controller
public class RegistrationController {

    UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String showRegistrationForm(WebRequest request, Model model) {
        AccountDto userDto = new AccountDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount
            (@ModelAttribute("user") @Valid AccountDto accountDto,
             BindingResult result, Model model) {

        User registered = new User();
        if (!result.hasErrors()) {
            registered = createUserAccount(accountDto, result);
        }

        if (registered == null) {
            result.rejectValue("email", "error.message.emailExists");
        }
        model.addAttribute("user", accountDto);

        if (result.hasErrors()) {
            return "registration";
        }
        return "successRegister";
    }

    private User createUserAccount(AccountDto accountDto, BindingResult result) {
        User registered = null;
        try {
            registered = userService.registerNewUserAccount(accountDto);
        } catch (EmailExistsException e) {
            return null;
        }
        return registered;
    }
}
