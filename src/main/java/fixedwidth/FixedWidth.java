package fixedwidth;

import java.util.BitSet;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * The main api for the fixedwidth library.
 *
 * @param <T> the type of objects to produce
 */
public class FixedWidth<T> {

    private final Class<T> clazz;
    private final int maxPosition;
    private final ScanResult scanResult;

    private FixedWidth(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz);
        try {
            scanResult = new Scanner(clazz).scan();

            maxPosition = getMaxMappedPosition(clazz);

            if (!scanResult.getRecord().allowUnmapped())
                checkUnmappedPositions();

            if (!scanResult.getRecord().allowOverlapping())
                checkOverlapping();
        } catch (RecordDefinitionException e) {
            throw e;
        } catch (Exception e) {
            throw RecordDefinitionException.somethingWentWrong(e);
        }
    }

    private int getMaxMappedPosition(Class<T> clazz) {
        IntStream streams = IntStream.concat(
                scanResult.getMappings().stream().mapToInt(Mapping::getEnd),
                scanResult.getFillers().stream().mapToInt(ParsedFiller::getEnd)
        );
        return streams
                .max()
                .orElseThrow(() -> RecordDefinitionException.missingFields(clazz));
    }

    private void checkOverlapping() {
        BitSet set = new BitSet(maxPosition);
        for (ParsedFiller filler : scanResult.getFillers()) {
            checkOverlapping(
                    set,
                    filler.getStart(),
                    filler.getEnd(),
                    () -> String.format(
                            "Duplicate mapping. The filler with start %d, end %d, character '%s' and regex '%s' tries to map to previously mapped positions",
                            filler.getStart(),
                            filler.getEnd(),
                            filler.getCharacter(),
                            filler.getPattern() == null ? "null" : filler.getPattern().pattern()
                    ));
        }

        for (Mapping mapping : scanResult.getMappings()) {
            checkOverlapping(
                    set,
                    mapping.getStart(),
                    mapping.getEnd(),
                    () -> String.format(
                            "Duplicate mapping. The mapping on field %s on class %s tries to map to previously mapped positions",
                            mapping.getField().getName(),
                            mapping.getField().getDeclaringClass().getName()
                    ));
        }
    }

    private void checkOverlapping(BitSet set, int start, int end, Supplier<String> exceptionMessageSupplier) {
        BitSet usedSet = new BitSet();
        usedSet.set(start, end);

        if (set.intersects(usedSet)) {
            throw new RecordDefinitionException(exceptionMessageSupplier.get());
        }

        set.or(usedSet);
    }

    /**
     * Parses the given line and returns a new instance of {@link T} with the filled in values.
     * Throws an {@link ParseException} when at input can't be parsed for the given mappings.
     *
     * @param line the input to parsed (non-null)
     * @return the object with mapped values
     * @throws ParseException for invalid input
     */
    public T parse(String line) throws ParseException {
        if (line.length() < maxPosition) {
            throw new ParseException(String.format("Line length %d is less than the max mapped position %d on class %s",
                    line.length(),
                    maxPosition,
                    clazz
            ));
        }
        T instance = ReflectUtil.createInstance(clazz, e -> new ParseException(
                String.format(
                        "Unable to create instance on class %s",
                        clazz.getName()
                ), e));

        checkFillers(line);
        applyMappings(line, instance);


        return instance;
    }

    private void applyMappings(String line, T instance) {
        for (Mapping mapping : scanResult.getMappings()) {
            String substring = line.substring(mapping.getStart(), mapping.getEnd());
            try {
                mapping.getField().set(instance, mapping.getConverter().apply(substring));
            } catch (Exception e) {
                throw new ParseException(String.format(
                        "Unable to parse line '%s', an exception occurred for field %s on substring '%s'", line, mapping.getField().getName(), substring
                ), e);
            }
        }
    }

    private void checkFillers(String line) {
        for (ParsedFiller filler : scanResult.getFillers()) {
            String substring = line.substring(filler.getStart(), filler.getEnd());
            if (filler.getPattern() != null) {
                if (!filler.getPattern().matcher(substring).matches()) {
                    throw new ParseException("");
                }
            } else {
                char[] chars = substring.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    if (c != filler.getCharacter()) {
                        throw ParseException.fillerMismatch(filler, c,i, clazz);
                    }
                }
            }
        }
    }

    /**
     * Scans the given class for mappings which can be handled by fixedwidth. The given class must have the
     * {@link fixedwidth.annotations.Record} annotation and at least one field annotated with
     * {@link fixedwidth.annotations.Position}. The result can be reused to parse multiple lines but is not
     * threadsafe. An {@link fixedwidth.RecordDefinitionException} is thrown when an invalid mapping is found.
     *
     * @param clazz the class to scan (non-null)
     * @param <T>   the class to scan
     * @return an instance of fixedwidth
     * @throws RecordDefinitionException on an invalid-mapping
     */
    public static <T> FixedWidth<T> forClass(Class<T> clazz) throws RecordDefinitionException {
        return new FixedWidth<>(clazz);
    }

    private void checkUnmappedPositions() {
        BitSet set = new BitSet(maxPosition);

        for (Mapping mapping : scanResult.getMappings()) {
            set.set(mapping.getStart(), mapping.getEnd());
        }

        for (ParsedFiller filler : scanResult.getFillers()) {
            set.set(filler.getStart(), filler.getEnd());
        }

        set.flip(0, maxPosition);

        if (!set.isEmpty()) {
            throw RecordDefinitionException.forUnmapped(set.toString());
        }
    }

}
