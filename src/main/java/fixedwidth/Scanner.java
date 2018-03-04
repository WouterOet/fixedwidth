package fixedwidth;

import fixedwidth.annotations.Converter;
import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class Scanner {

    private static final Map<Class<?>, Function<String, Object>> SUPPORTED_TYPES = new HashMap<>();

    static {
        SUPPORTED_TYPES.put(Long.class, Long::parseLong);
        SUPPORTED_TYPES.put(long.class, Long::parseLong);
        SUPPORTED_TYPES.put(Integer.class, Integer::parseInt);
        SUPPORTED_TYPES.put(int.class, Integer::parseInt);
        SUPPORTED_TYPES.put(Short.class, Short::parseShort);
        SUPPORTED_TYPES.put(short.class, Short::parseShort);
        SUPPORTED_TYPES.put(String.class, s -> s);
        SUPPORTED_TYPES.put(LocalDateTime.class, LocalDateTime::parse);
        SUPPORTED_TYPES.put(LocalDate.class, LocalDate::parse);
        SUPPORTED_TYPES.put(Double.class, Double::parseDouble);
        SUPPORTED_TYPES.put(double.class, Double::parseDouble);
        SUPPORTED_TYPES.put(Float.class, Float::parseFloat);
        SUPPORTED_TYPES.put(float.class, Float::parseFloat);
    }

    private final Class<?> clazz;

    Scanner(Class<?> clazz) {
        this.clazz = clazz;
    }

    List<Mapping> scan() {
        if (clazz.getAnnotation(Record.class) == null) {
            throw RecordDefinitionException.forMissingRecord(clazz);
        }

        ReflectUtil.createInstance(clazz, e -> RecordDefinitionException.newInstanceProblem(clazz, e));

        Field[] fields = clazz.getDeclaredFields();

        if (fields.length == 0) {
            throw RecordDefinitionException.missingFields(clazz);
        }

        return Arrays.stream(fields)
                .map(this::createMapping)
                .collect(Collectors.toList());
    }

    private Mapping createMapping(Field field) {
        field.setAccessible(true);
        Position position = field.getAnnotation(Position.class);
        if (position == null) {
            throw RecordDefinitionException.forMissingPosition(field);
        }
        if (position.start() >= position.end()) {
            throw RecordDefinitionException.invalidPosition(field);
        }

        Converter converter = field.getAnnotation(Converter.class);
        if (converter != null) {
            return useConverter(field, position, converter);
        }

        Function<String, Object> function = SUPPORTED_TYPES.get(field.getType());
        if (function == null) {
            throw RecordDefinitionException.unsupportedType(field);
        }

        return new Mapping(field, position.start(), position.end(), function);
    }

    private Mapping useConverter(Field field, Position position, Converter converter) {
        Object instance = ReflectUtil.createInstance(converter.value(), e -> new RecordDefinitionException("Unable to create converter", e));
        try {
            Function<String, Object> function = (Function<String, Object>) instance;
            return new Mapping(field, position.start(), position.end(), function);
        } catch (ClassCastException e) {
            throw RecordDefinitionException.invalidConverter(converter.value(), field);
        }
    }
}
