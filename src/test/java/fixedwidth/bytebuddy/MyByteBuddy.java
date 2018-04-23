package fixedwidth.bytebuddy;

import fixedwidth.annotations.Filler;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;

import java.util.ArrayList;
import java.util.List;

public class MyByteBuddy {

    private DynamicType.Builder<Object> builder;
    private char fieldName = 'a';
    private final List<FillerImpl> fillers = new ArrayList<>();

    private MyByteBuddy() {
        builder = new ByteBuddy().subclass(Object.class);
    }

    public static MyByteBuddy createClass() {
        return new MyByteBuddy();
    }

    public MyByteBuddy addRecord() {
        return addRecord(false, false);
    }

    public MyByteBuddy addRecord(boolean allowOverlapping, boolean allowUnmapped) {
        builder = builder.annotateType(new RecordImpl(allowOverlapping, allowUnmapped));
        return this;
    }

    public MyByteBuddy addFiller(int start, int end, char c) {
        return addFiller(start, end, c, "");
    }

    public MyByteBuddy addFiller(int start, int end, String pattern) {
        return addFiller(start, end, '\u0000', pattern);
    }

    public MyByteBuddy addFiller(int start, int end, char c, String pattern) {
        fillers.add(new FillerImpl(start, end, c, pattern));
        return this;
    }


    public MyByteBuddy addStandardField() {
        return addStandardField(0, 1);
    }

    public MyByteBuddy addStandardField(int start, int end) {
        return addStandardField(String.valueOf(fieldName++), start, end);
    }


    public MyByteBuddy addStandardField(String name, int start, int end) {
        builder = builder.defineField(name, String.class, Visibility.PRIVATE)
                .annotateField(new PositionImpl(start, end));
        return this;
    }

    public Class<?> build() {
        if (fillers.size() > 0) {
            if (fillers.size() == 1) {
                builder = builder.annotateType(fillers.get(0));
            } else {
                builder = builder.annotateType(new FillersImpl(fillers.toArray(new Filler[fillers.size()])));
            }
        }
        return builder.make().load(this.getClass().getClassLoader()).getLoaded();
    }

    public MyByteBuddy addField(String fieldName, Class<?> type, int start, int end) {
        return addField(fieldName, type, start, end, null);
    }

    public MyByteBuddy addField(String fieldName, Class<?> type, int start, int end, Class<?> converter) {
        DynamicType.Builder.FieldDefinition.Optional<Object> field = builder.defineField(fieldName, type, Visibility.PRIVATE)
                .annotateField(new PositionImpl(start, end));
        if (converter != null)
            builder = field.annotateField(new ConverterImpl(converter));
        else {
            builder = field;
        }
        return this;
    }
}
