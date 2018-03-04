package fixedwidth;

import java.lang.reflect.Field;

public class RecordDefinitionException extends RuntimeException {

    public RecordDefinitionException(String message) {
        super(message);
    }

    public RecordDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public static RecordDefinitionException forMissingRecord(Class<?> clazz) {
        return new RecordDefinitionException(
                String.format(
                        "The class %s is missing the fixedwidth.annotions.Record annotation",
                        clazz.getName()
                )
        );
    }

    public static RecordDefinitionException forMissingPosition(Field field) {
        return new RecordDefinitionException(
                String.format(
                        "The field %s on class %s is missing the annotation fixedwidth.annotations.Position",
                        field.getName(),
                        field.getDeclaringClass().getName()
                )
        );
    }

    public static RecordDefinitionException newInstanceProblem(Class<?> clazz, ReflectiveOperationException e) {
        return new RecordDefinitionException(
                String.format(
                        "Unable to create a new instance of class %s",
                        clazz.getName()
                ), e
        );
    }

    public static RecordDefinitionException invalidPosition(Field field) {
        return new RecordDefinitionException(
                String.format(
                        "The field %s on class %s has an invalid fixedwidth.annotations.Position combination. The end needs to be after the start",
                        field.getName(),
                        field.getDeclaringClass().getName()
                )
        );
    }

    public static RecordDefinitionException missingFields(Class<?> clazz) {
        return new RecordDefinitionException(
                String.format(
                        "The class %s has no mappable fields",
                        clazz.getName()
                )
        );
    }
}
