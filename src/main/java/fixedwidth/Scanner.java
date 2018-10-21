package fixedwidth;

import fixedwidth.annotations.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fixedwidth.Util.requireAnnotation;

class Scanner {

    private final Types types = new Types();
    private final Class<?> clazz;

    Scanner(Class<?> clazz) {
        this.clazz = clazz;
    }

    ScanResult scan() {
        Record record = clazz.getAnnotation(Record.class);
        if (record == null) {
            throw RecordDefinitionException.forMissingRecord(clazz);
        }

        // Check if an instance can be created
        Util.createInstance(clazz, e -> RecordDefinitionException.newInstanceProblem(clazz, e));

        List<Mapping> mappings = getMappings();
        List<ParsedFiller> fillers = getFillers();

        return new ScanResult(mappings, fillers, record);
    }

    private List<Mapping> getMappings() {
        Field[] fields = clazz.getDeclaredFields();

        if (fields.length == 0) {
            throw RecordDefinitionException.missingFields(clazz);
        }

        return Arrays.stream(fields)
                .map(this::createMapping)
                .collect(Collectors.toList());
    }

    private List<ParsedFiller> getFillers() {
        Fillers wrapper = clazz.getAnnotation(Fillers.class);
        Filler filler = clazz.getAnnotation(Filler.class);

        List<Filler> fillers;
        if(wrapper != null) {
            fillers = Arrays.asList(wrapper.value());
        } else if(filler != null) {
            fillers = Collections.singletonList(filler);
        } else {
            fillers = Collections.emptyList();
        }

        return fillers
                .stream()
                .map(f -> ParsedFiller.from(f, clazz))
                .collect(Collectors.toList());
    }

    private Mapping createMapping(Field field) {
        field.setAccessible(true);
        Position position = getPosition(field);

        Converter converter = field.getAnnotation(Converter.class);
        if (converter != null) {
            return useConverter(field, position, converter);
        }

        var contentFunction = types.getSupported().entrySet()
                .stream()
                .filter(entry -> entry.getKey().apply(field.getType()))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(f -> f.apply(field))
                .orElseThrow(() -> RecordDefinitionException.unsupportedType(field));

        return new Mapping(field, position.start(), position.end(), contentFunction);
    }

    private Position getPosition(Field field) {
        Position position = requireAnnotation(field, Position.class, () -> RecordDefinitionException.forMissingPosition(field));
        if (position.start() >= position.end()) {
            throw RecordDefinitionException.invalidPosition(field);
        }
        return position;
    }

    private Mapping useConverter(Field field, Position position, Converter converter) {
        Object instance = Util.createInstance(converter.value(), e -> new RecordDefinitionException("Unable to create converter", e));
        try {
            Function<String, Object> function = (Function<String, Object>) instance;
            return new Mapping(field, position.start(), position.end(), function);
        } catch (ClassCastException e) {
            throw RecordDefinitionException.invalidConverter(converter.value(), field);
        }
    }
}
