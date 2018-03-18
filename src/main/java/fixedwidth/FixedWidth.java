package fixedwidth;

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
                .get(); // Safe get. Always one or more mappings
    }

    public T parse(String line) throws ParseException {
        if (line.length() < maxMapping.getEnd()) {
            throw new ParseException(String.format("Line length is less than the position end of field %s on class %s",
                    maxMapping.getField().getName(),
                    maxMapping.getField().getDeclaringClass().getName()
            ));
        }
        T instance = ReflectUtil.createInstance(clazz, e -> new ParseException(
                String.format(
                        "Unable to create instance on class %s",
                        clazz.getName()
                ), e));

        for (Mapping mapping : mappings) {
            try {
                String substring = line.substring(mapping.getStart(), mapping.getEnd());
                mapping.getField().set(instance, mapping.getConverter().apply(substring));
            } catch (Exception e) {
                throw new ParseException(String.format(
                        "Unable to parse line '%s', an exception occurred for field %s", line, mapping.getField().getName()
                ), e);
            }
        }


        return instance;
    }

    public static <T> FixedWidth<T> forClass(Class<T> clazz) throws RecordDefinitionException {
        Objects.requireNonNull(clazz);
        return new FixedWidth<>(clazz, new Scanner(clazz).scan());
    }
}
