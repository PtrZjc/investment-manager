package pl.zajacp.investmentmanager.user.validation;

public class LoginExistsException extends Exception {

    public LoginExistsException(final String message) {
        super(message);
    }
}
