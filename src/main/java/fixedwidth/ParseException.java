package fixedwidth;

public class ParseException extends RuntimeException {

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    static ParseException fillerMismatch(ParsedFiller filler, char foundChar, int index, Class<?> clazz) {
        return new ParseException(String.format(
                "Filler mismatch. Expected character '%c', found '%c' on index %d on class %s",
                filler.getCharacter(),
                foundChar,
                index,
                clazz.getName()
        ));
    }
}
