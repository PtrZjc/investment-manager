package pl.zajacp.investmentmanager.user;

import lombok.Data;
import pl.zajacp.investmentmanager.user.registration.validation.PasswordMatches;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@PasswordMatches
public class UserDto {

    private final static String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    @NotNull
    @Size(min=1, max=20)
    private String name;

    @NotNull
    @Size(min=3, max=15)
    @Pattern(regexp="[a-zA-Z0-9\\-\\_]+")
    private String login;

    @NotNull
    @Size(min=3)
    private String password;
    private String matchingPassword;

    @NotNull
    @Pattern(regexp=emailRegex)
    private String email;


}
