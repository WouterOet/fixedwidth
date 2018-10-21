package fixedwidth;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;

class Util {

    private Util(){}

    static <T extends Annotation> T requireAnnotation(Field field, Class<T> clazz, Supplier<RecordDefinitionException> supplier) {
        T annotation = field.getAnnotation(clazz);
        if (annotation == null) {
            throw supplier.get();
        }
        return annotation;
    }

    static <T, E extends Exception> T createInstance(Class<T> clazz, Function<ReflectiveOperationException, E> exceptionFunction) throws E {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw exceptionFunction.apply(e);
        }
    }
}
