package fixedwidth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

class ReflectUtil {

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
