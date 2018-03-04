package fixedwidth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FixedWidth<T> {

    private final Class<T> clazz;
    private final List<Mapping> mappings;
    private final Mapping maxMapping;

    private FixedWidth(Class<T> clazz, List<Mapping> mappings) {
        this.clazz = clazz;
        this.mappings = mappings;

        maxMapping = mappings
                .stream()
                .max(Comparator.comparingInt(Mapping::getEnd))
                .get();
    }

    public T parse(String line) throws ParseException {
        if (line.length() < maxMapping.getEnd()) {
            throw new ParseException(String.format("Line length is less than the position end of field %s on class %s",
                    maxMapping.getField().getName(),
                    maxMapping.getField().getDeclaringClass().getName()
            ));
        }
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();

            for (Mapping mapping : mappings) {
                String substring = line.substring(mapping.getStart(), mapping.getEnd());
                mapping.getField().set(instance, mapping.getConverter().apply(substring));
            }

            return instance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ParseException("Unable to create instance", e);
        }
    }

    public static <T> FixedWidth<T> forClass(Class<T> clazz) throws RecordDefinitionException {
        Objects.requireNonNull(clazz);
        return new FixedWidth<>(clazz, new Scanner(clazz).scan());
    }
}
