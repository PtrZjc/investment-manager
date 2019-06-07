package pl.zajacp.investmentmanager.user.validation;

public class LoginExistsException extends Throwable {

    public LoginExistsException(final String message) {
        super(message);
    }

}
