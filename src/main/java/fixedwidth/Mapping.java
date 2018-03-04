package fixedwidth;

import java.lang.reflect.Field;
import java.util.function.Function;

class Mapping {
    private final Field field;
    private final int start;
    private final int end;
    private final Function<String, Object> converter;

    Mapping(Field field, int start, int end, Function<String, Object> converter) {
        this.field = field;
        this.start = start;
        this.end = end;
        this.converter = converter;
    }

    public Field getField() {
        return field;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Function<String, Object> getConverter() {
        return converter;
    }
}
