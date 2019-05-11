package pl.zajacp.investmentmanager.user.registration.validation;

public class EmailExistsException extends Throwable {

    public EmailExistsException(final String message) {
        super(message);
    }

}
