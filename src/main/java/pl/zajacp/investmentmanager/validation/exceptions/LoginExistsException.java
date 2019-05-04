package pl.zajacp.investmentmanager.validation.exceptions;

public class LoginExistsException extends Throwable {

    public LoginExistsException(final String message) {
        super(message);
    }

}
