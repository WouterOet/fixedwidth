package fixedwidth;

import fixedwidth.annotations.Converter;
import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;
import fixedwidth.annotations.WithPattern;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class Scanner {

    private static final Map<Function<Class<?>, Boolean>, Function<Field, Function<String, Object>>> SUPPORTED_TYPES = new HashMap<>();

    private static Function<Field, Function<String, Object>> withPatternContent(BiFunction<String, DateTimeFormatter, Object> consumer, DateTimeFormatter formatter) {
        return field -> {
            WithPattern pattern = field.getAnnotation(WithPattern.class);
            if (pattern != null) {
                return s -> consumer.apply(s, DateTimeFormatter.ofPattern(pattern.value()));
            } else {
                return s -> consumer.apply(s, formatter);
            }
        };
    }

    static {
        Function<Function<String, Object>, Function<Field, Function<String, Object>>> withoutContext = i -> f -> i;
        Function<Class<?>, Function<Class<?>, Boolean>> fixedClass = fixed -> actual -> fixed == actual;

        SUPPORTED_TYPES.put(fixedClass.apply(Long.class), withoutContext.apply(Long::parseLong));
        SUPPORTED_TYPES.put(fixedClass.apply(long.class), withoutContext.apply(Long::parseLong));
        SUPPORTED_TYPES.put(fixedClass.apply(Integer.class), withoutContext.apply(Integer::parseInt));
        SUPPORTED_TYPES.put(fixedClass.apply(int.class), withoutContext.apply(Integer::parseInt));
        SUPPORTED_TYPES.put(fixedClass.apply(Short.class), withoutContext.apply(Short::parseShort));
        SUPPORTED_TYPES.put(fixedClass.apply(short.class), withoutContext.apply(Short::parseShort));
        SUPPORTED_TYPES.put(fixedClass.apply(String.class), withoutContext.apply(s -> s));
        SUPPORTED_TYPES.put(fixedClass.apply(Double.class), withoutContext.apply(Double::parseDouble));
        SUPPORTED_TYPES.put(fixedClass.apply(double.class), withoutContext.apply(Double::parseDouble));
        SUPPORTED_TYPES.put(fixedClass.apply(Float.class), withoutContext.apply(Float::parseFloat));
        SUPPORTED_TYPES.put(fixedClass.apply(float.class), withoutContext.apply(Float::parseFloat));
        SUPPORTED_TYPES.put(fixedClass.apply(Boolean.class), withoutContext.apply(Boolean::parseBoolean));
        SUPPORTED_TYPES.put(fixedClass.apply(boolean.class), withoutContext.apply(Boolean::parseBoolean));
        SUPPORTED_TYPES.put(fixedClass.apply(byte.class), withoutContext.apply(Byte::parseByte));
        SUPPORTED_TYPES.put(fixedClass.apply(Byte.class), withoutContext.apply(Byte::parseByte));
        SUPPORTED_TYPES.put(fixedClass.apply(char.class), withoutContext.apply(s -> s.charAt(0)));
        SUPPORTED_TYPES.put(fixedClass.apply(Character.class), withoutContext.apply(s -> s.charAt(0)));

        SUPPORTED_TYPES.put(fixedClass.apply(LocalDateTime.class), withPatternContent(LocalDateTime::parse, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        SUPPORTED_TYPES.put(fixedClass.apply(LocalDate.class), withPatternContent(LocalDate::parse, DateTimeFormatter.ISO_LOCAL_DATE));

        SUPPORTED_TYPES.put(Class::isEnum, field -> value -> Enum.valueOf((Class) field.getType(), value));

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
        Position position = getPosition(field);

        Converter converter = field.getAnnotation(Converter.class);
        if (converter != null) {
            return useConverter(field, position, converter);
        }

        Function<Field, Function<String, Object>> contentFunction = SUPPORTED_TYPES.entrySet()
                .stream()
                .filter(entry -> entry.getKey().apply(field.getType()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> RecordDefinitionException.unsupportedType(field));

        return new Mapping(field, position.start(), position.end(), contentFunction.apply(field));
    }

    private Position getPosition(Field field) {
        Position position = field.getAnnotation(Position.class);
        if (position == null) {
            throw RecordDefinitionException.forMissingPosition(field);
        }
        if (position.start() >= position.end()) {
            throw RecordDefinitionException.invalidPosition(field);
        }
        return position;
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
