package interaction.api.exception;

public class UserOperationFailedException extends RuntimeException {
    public UserOperationFailedException(String message) {
        super(message);
    }
}
