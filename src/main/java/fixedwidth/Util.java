package fixedwidth;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Supplier;

class Util {

    private Util(){}

    static <T extends Annotation> T requireAnnotation(Field field, Class<T> annotation, Supplier<RecordDefinitionException> supplier) {
        T pattern = field.getAnnotation(annotation);
        if (pattern == null) {
            throw supplier.get();
        }
        return pattern;
    }

}
