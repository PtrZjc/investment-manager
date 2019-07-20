package pl.zajacp.investmentmanager.user.validation;

public class EmailExistsException extends Exception {

    public EmailExistsException(final String message) {
        super(message);
    }

}
