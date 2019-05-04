package pl.zajacp.investmentmanager.registration;

import lombok.Data;
import pl.zajacp.investmentmanager.validation.PasswordMatches;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@PasswordMatches
public class AccountDto {

    @NotNull
    @Size(min=5, max=20)
    private String name;

    @NotNull
    @Size(min=3)
    private String password;
    private String matchingPassword;

    @NotNull
    @Pattern(regexp=".+@.+\\..+")
    private String email;
}
