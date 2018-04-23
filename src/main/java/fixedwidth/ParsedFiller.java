package fixedwidth;

import fixedwidth.annotations.Filler;
import fixedwidth.annotations.Position;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class ParsedFiller {

    private final int start;
    private final int end;
    private final Pattern pattern;
    private final char character;

    private ParsedFiller(int start, int end, Pattern pattern, char character) {
        this.start = start;
        this.end = end;
        this.pattern = pattern;
        this.character = character;
    }


    static ParsedFiller from(Filler filler, Class<?> clazz) {
        Position position = filler.position();
        int start = position.start();
        int end = position.end();

        if (start >= end) {
            throw RecordDefinitionException.invalidPosition(filler, clazz);
        }

        String regex = filler.regex();
        char character = filler.character();

        if(regex.equals("") && character == '\u0000') {
            throw RecordDefinitionException.missingFillerContent(filler, clazz);
        }

        if(!regex.equals("") && character != '\u0000') {
            throw RecordDefinitionException.duplicateFillerContent(filler, clazz);
        }

        if (!"".equals(regex)) {
            try {
                return new ParsedFiller(start, end, Pattern.compile(regex), (char) -1);
            } catch (PatternSyntaxException e) {
                throw RecordDefinitionException.invalidPattern(filler, clazz, e);
            }
        } else {
            return new ParsedFiller(start, end, null, character);
        }
    }

    int getStart() {
        return start;
    }

    int getEnd() {
        return end;
    }

    Pattern getPattern() {
        return pattern;
    }

    char getCharacter() {
        return character;
    }
}
