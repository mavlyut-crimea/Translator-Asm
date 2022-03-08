package myBase;

public class NotImplementedException extends RuntimeException {
    public NotImplementedException(String message) {
        super(String.format("An operation is not implemented: %s", message));
    }
}
