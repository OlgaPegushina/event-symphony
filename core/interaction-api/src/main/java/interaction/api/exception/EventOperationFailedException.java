package interaction.api.exception;

public class EventOperationFailedException extends RuntimeException {
    public EventOperationFailedException(String message) {
        super(message);
    }
}
