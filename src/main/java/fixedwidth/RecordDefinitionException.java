package fixedwidth;

import java.lang.reflect.Field;

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
                        "The field %s on class %s has an unsupported type. Maybe as the @Converter annotation.",
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
}
