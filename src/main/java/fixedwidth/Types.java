package fixedwidth;

import fixedwidth.annotations.WithPattern;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static fixedwidth.Util.requireAnnotation;

class Types {

    private final Map<Function<Class<?>, Boolean>, Function<Field, Function<String, Object>>> map;

    Types() {
        Map<Function<Class<?>, Boolean>, Function<Field, Function<String, Object>>> map = new HashMap<>();

        addType(map, Long.class, Long::parseLong);
        addType(map, long.class, Long::parseLong);
        addType(map, Integer.class, Integer::parseInt);
        addType(map, int.class, Integer::parseInt);
        addType(map, Short.class, Short::parseShort);
        addType(map, short.class, Short::parseShort);
        addType(map, String.class, s -> s);
        addType(map, Double.class, Double::parseDouble);
        addType(map, double.class, Double::parseDouble);
        addType(map, Float.class, Float::parseFloat);
        addType(map, float.class, Float::parseFloat);
        addType(map, Boolean.class, Boolean::parseBoolean);
        addType(map, boolean.class, Boolean::parseBoolean);
        addType(map, byte.class, Byte::parseByte);
        addType(map, Byte.class, Byte::parseByte);
        addType(map, char.class, s -> s.charAt(0));
        addType(map, Character.class, s -> s.charAt(0));

        map.put(fixedClass(LocalDateTime.class), withPatternContext(LocalDateTime::parse));
        map.put(fixedClass(LocalDate.class), withPatternContext(LocalDate::parse));
        map.put(fixedClass(LocalTime.class), withPatternContext(LocalTime::parse));
        Function<Field, Function<String, Object>> dateFunc = withPatternContext(LocalDateTime::parse)
;//                .andThen(f -> f.andThen(ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())));
        map.put(fixedClass(Date.class), dateFunc);

        map.put(Class::isEnum, field -> value -> Enum.valueOf((Class) field.getType(), value));

        this.map = Collections.unmodifiableMap(map);
    }

    private <T> Function<Field, Function<String, T>> withPatternContext(BiFunction<String, DateTimeFormatter, T> converter) {
        return field -> {
            WithPattern pattern = requireAnnotation(field, WithPattern.class, () -> RecordDefinitionException.missingPattern(field));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern.pattern());
            return s -> converter.apply(s, formatter);
        };
    }

    private Function<Field, Function<String, Object>> withoutContext(Function<String, Object> converter) {
        return f -> converter;
    }

    private Function<Class<?>, Boolean> fixedClass(Class<?> supportedClass) {
        return foundClass -> foundClass == supportedClass;
    }

    private void addType(Map<Function<Class<?>, Boolean>, Function<Field, Function<String, Object>>> map, Class<?> supportedClass, Function<String, Object> converter) {
        map.put(fixedClass(supportedClass), withoutContext(converter));
    }

    Map<Function<Class<?>, Boolean>, Function<Field, Function<String, Object>>> getSupported() {
        return map;
    }

}
