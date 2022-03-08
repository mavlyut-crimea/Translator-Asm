package parsers;

public class ParserException extends RuntimeException {
    protected ParserException(final String sectionName, final String message, final String cause) {
        super(String.format(
                "%s while parsing \"%s\" section: %s",
                message, sectionName, cause
        ));
    }

    protected ParserException(final String type, final String message) {
        super(String.format("Unknown \"%s\"-type instruction: %s", type, message));
    }

    protected ParserException(final String message) {
        super(String.format("Can't start parse ELF-file: %s", message));
    }
}
