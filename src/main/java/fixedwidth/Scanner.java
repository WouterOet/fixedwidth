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

    private static final Map<Class<?>, Function<Field, Function<String, Object>>> SUPPORTED_TYPES = new HashMap<>();

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

        SUPPORTED_TYPES.put(Long.class, withoutContext.apply(Long::parseLong));
        SUPPORTED_TYPES.put(long.class, withoutContext.apply(Long::parseLong));
        SUPPORTED_TYPES.put(Integer.class, withoutContext.apply(Integer::parseInt));
        SUPPORTED_TYPES.put(int.class, withoutContext.apply(Integer::parseInt));
        SUPPORTED_TYPES.put(Short.class, withoutContext.apply(Short::parseShort));
        SUPPORTED_TYPES.put(short.class, withoutContext.apply(Short::parseShort));
        SUPPORTED_TYPES.put(String.class, withoutContext.apply(s -> s));
        SUPPORTED_TYPES.put(Double.class, withoutContext.apply(Double::parseDouble));
        SUPPORTED_TYPES.put(double.class, withoutContext.apply(Double::parseDouble));
        SUPPORTED_TYPES.put(Float.class, withoutContext.apply(Float::parseFloat));
        SUPPORTED_TYPES.put(float.class, withoutContext.apply(Float::parseFloat));

        SUPPORTED_TYPES.put(LocalDateTime.class, withPatternContent(LocalDateTime::parse, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        SUPPORTED_TYPES.put(LocalDate.class, withPatternContent(LocalDate::parse, DateTimeFormatter.ISO_LOCAL_DATE));

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

        Function<Field, Function<String, Object>> contentFunction = SUPPORTED_TYPES.get(field.getType());
        if (contentFunction == null) {
            throw RecordDefinitionException.unsupportedType(field);
        }

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
