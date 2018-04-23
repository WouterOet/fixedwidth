package fixedwidth;

import fixedwidth.annotations.Filler;

import java.lang.reflect.Field;
import java.util.regex.PatternSyntaxException;

public class RecordDefinitionException extends RuntimeException {

    public RecordDefinitionException(String message) {
        super(message);
    }

    public RecordDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }


    static RecordDefinitionException forMissingRecord(Class<?> clazz) {
        return new RecordDefinitionException(
                String.format(
                        "The class %s is missing the fixedwidth.annotions.Record annotation",
                        clazz.getName()
                )
        );
    }

    static RecordDefinitionException forMissingPosition(Field field) {
        return new RecordDefinitionException(
                String.format(
                        "The field %s on class %s is missing the annotation fixedwidth.annotations.Position",
                        field.getName(),
                        field.getDeclaringClass().getName()
                )
        );
    }

    static RecordDefinitionException newInstanceProblem(Class<?> clazz, ReflectiveOperationException e) {
        return new RecordDefinitionException(
                String.format(
                        "Unable to create a new instance of class %s",
                        clazz.getName()
                ), e
        );
    }

    static RecordDefinitionException invalidPosition(Field field) {
        return new RecordDefinitionException(
                String.format(
                        "The field %s on class %s has an invalid fixedwidth.annotations.Position combination. The end needs to be after the start",
                        field.getName(),
                        field.getDeclaringClass().getName()
                )
        );
    }

    static RecordDefinitionException missingFields(Class<?> clazz) {
        return new RecordDefinitionException(
                String.format(
                        "The class %s has no mappable fields",
                        clazz.getName()
                )
        );
    }

    static RecordDefinitionException unsupportedType(Field field) {
        return new RecordDefinitionException(
                String.format(
                        "The field %s on class %s has an unsupported type. Maybe use the @Converter annotation.",
                        field.getName(),
                        field.getDeclaringClass().getName()
                )
        );
    }

    static RecordDefinitionException invalidConverter(Class<?> value, Field field) {
        return new RecordDefinitionException(
                String.format(
                        "The class %s is does not implement Function<String, Object>. See field %s on class %s.",
                        value.getName(),
                        field.getName(),
                        field.getDeclaringClass().getName()
                )
        );
    }

    static RecordDefinitionException forUnmapped(String positions) {
        return new RecordDefinitionException(
                "There are unmapped positions in the mappings. The following positions don't have a mapping: " + positions
        );
    }

    static RecordDefinitionException invalidPosition(Filler filler, Class<?> clazz) {
        return new RecordDefinitionException(
                String.format(
                        "The %s on class %s has an invalid fixedwidth.annotations.Filler combination. The end needs to be after the start",
                        toString(filler),
                        clazz.getName()
                )
        );
    }

    private static String toString(Filler filler) {
        return String.format("Filler(start=%d, end=%d, character=%c, regex=%s)",
                filler.position().start(),
                filler.position().end(),
                filler.character(),
                filler.regex()
        );
    }

    static RecordDefinitionException missingFillerContent(Filler filler, Class<?> clazz) {
        return new RecordDefinitionException(
                String.format("The %s on class %s doesn't have a valid content",
                        toString(filler),
                        clazz.getName()))
                ;
    }

    static RecordDefinitionException duplicateFillerContent(Filler filler, Class<?> clazz) {
        return new RecordDefinitionException("");
    }

    static RecordDefinitionException invalidPattern(Filler filler, Class<?> clazz, PatternSyntaxException e) {
        return new RecordDefinitionException("", e);
    }

    static RecordDefinitionException missingPattern(Field field) {
        return new RecordDefinitionException("");
    }

    static RecordDefinitionException somethingWentWrong(Exception e) {
        return new RecordDefinitionException("", e);
    }
}
