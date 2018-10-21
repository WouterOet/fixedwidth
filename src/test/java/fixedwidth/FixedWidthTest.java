package fixedwidth;

import fixedwidth.annotations.Converter;
import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;
import fixedwidth.annotations.WithPattern;
import fixedwidth.bytebuddy.MyByteBuddy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.BufferedReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class FixedWidthTest {

    @Test
    void forClassNullCheck() {
        assertThrows(NullPointerException.class, () -> FixedWidth.forClass(null));
    }

    @Test
    void lineLength() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField()
                .build();
        assertThrows(ParseException.class, () -> FixedWidth.forClass(clazz).parse(""));
    }

    private static class MyConverter implements Function<String, Object> {

        @Override
        public Integer apply(String s) {
            assertEquals("expected", s);
            return 1;
        }
    }

    @Record
    private static class ConverterTestCLass {
        @Position(start = 0, end = 8)
        @Converter(MyConverter.class)
        private Integer i;
    }

    @Test
    void converterCorrectlyInvoked() {
        ConverterTestCLass result = FixedWidth.forClass(ConverterTestCLass.class).parse("expected");
        assertNotNull(result);
        Integer expected = 1;
        assertEquals(expected, result.i);
    }

    private static class InvalidConverter {
    }

    @Test
    void converterIncorrectClass() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addField("i", int.class, 0, 2, InvalidConverter.class)
                .build();

        Executable executable = () -> FixedWidth.forClass(clazz);
        RecordDefinitionException exception = assertThrows(RecordDefinitionException.class, executable);
        assertTrue(exception.getMessage().startsWith("The class fixedwidth.FixedWidthTest$InvalidConverter is does not implement Function<String, Object>. See field i on class "));

    }

    private static class ConverterMismatch implements Function<String, Object> {
        @Override
        public Object apply(String s) {
            return "Not an integer";
        }
    }

    @Test
    void fieldConverterMismatch() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addField("i", Integer.class, 0, 1, ConverterMismatch.class)
                .build();

        FixedWidth<?> fixedWidth = FixedWidth.forClass(clazz);
        ParseException exception = assertThrows(ParseException.class, () -> fixedWidth.parse("123"));
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof IllegalArgumentException);
    }


    @Test
    void missingMappings() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField(8, 9)
                .build();
        RecordDefinitionException exception = assertThrows(RecordDefinitionException.class, () -> FixedWidth.forClass(clazz));
        String expected = "There are unmapped positions in the mappings. The following positions don't have a mapping: {0, 1, 2, 3, 4, 5, 6, 7}";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    void singleFiller() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addFiller(1, 8, '0')
                .addStandardField()
                .build();
        FixedWidth.forClass(clazz).parse("s0000000");
    }

    @Test
    void multipleFillers() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addFiller(1, 4, '0')
                .addFiller(4, 8, '1')
                .addStandardField()
                .build();
        FixedWidth.forClass(clazz).parse("s0001111");
    }

    @Test
    void overlappingMappings() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField("a", 0, 4)
                .addStandardField("b", 2, 6)
                .build();
        assertThrowsRDE(clazz);
    }

    @Test
    void overlappingFillers() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addFiller(0, 10, '0')
                .addFiller(7, 13, '1')
                .addStandardField()
                .build();
        assertThrowsDuplicateMapping(clazz);
    }

    private void assertThrowsDuplicateMapping(Class<?> clazz) {
        RecordDefinitionException exception = assertThrows(RecordDefinitionException.class, () -> FixedWidth.forClass(clazz));
        assertTrue(exception.getMessage().contains("Duplicate mapping"));
    }

    @Test
    void overlappingFillersMappings() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField(0, 5)
                .addFiller(3, 7, '0')
                .build();
        assertThrowsDuplicateMapping(clazz);
    }

    @Test
    void overlappingMappingsAllowed() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord(true, false)
                .addStandardField(0, 5)
                .addStandardField(3, 7)
                .build();
        FixedWidth.forClass(clazz);
    }

    @Test
    void overlappingFillersAllowed() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord(true, false)
                .addFiller(0, 5, '0')
                .addFiller(3, 7, '1')
                .addStandardField(7, 8)
                .build();
        FixedWidth.forClass(clazz);
    }

    @Test
    void overlappingFillersMappingsAllowed() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord(true, false)
                .addStandardField(0, 5)
                .addFiller(3, 5, '0')
                .build();
        FixedWidth.forClass(clazz);
    }

    @Test
    void unmapped() {
        Class<?> clazz = getClassWithUnmappedPositions(false);
        RecordDefinitionException exception = assertThrows(RecordDefinitionException.class, () -> FixedWidth.forClass(clazz));
        String expected = "There are unmapped positions in the mappings. The following positions don't have a mapping: {0, 4, 5, 8, 9}";
        assertEquals(expected, exception.getMessage());
    }

    private Class<?> getClassWithUnmappedPositions(boolean allowUnmapped) {
        return MyByteBuddy.createClass()
                .addRecord(false, allowUnmapped)
                .addStandardField(1, 4)
                .addStandardField(6, 8)
                .addFiller(10, 12, '0')
                .build();
    }

    @Test
    void unmappedAllowed() {
        Class<?> clazz = getClassWithUnmappedPositions(true);
        FixedWidth.forClass(clazz);
    }

    @Test
    void fillerWithIncorrectContent() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addFiller(0, 5, '0')
                .addField("s", String.class, 5, 6)
                .build();

        FixedWidth<?> fixedWidth = FixedWidth.forClass(clazz);
        assertThrows(ParseException.class, () -> fixedWidth.parse("10000ss"));
    }

    @Test
    void fillerWithPattern() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField(0, 1)
                .addFiller(1, 5, "[abc]*")
                .build();
        FixedWidth<?> fixedWidth = FixedWidth.forClass(clazz);
        fixedWidth.parse("1abba");
        assertThrows(ParseException.class, () -> fixedWidth.parse("1adda"));
    }

    @Test
    void invalidFillerEndBeforeStart() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField()
                .addFiller(5, 3, '0')
                .build();
        assertThrowsRDE(clazz);
    }

    private void assertThrowsRDE(Class<?> clazz) {
        assertThrows(RecordDefinitionException.class, () -> FixedWidth.forClass(clazz));
    }

    @Test
    void invalidFillerMissingContent() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField()
                .addFiller(1, 2, '\u0000', "") // Empty
                .build();

        assertThrowsRDE(clazz);
    }

    @Test
    void invalidFillerDuplicateContent() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField()
                .addFiller(1, 2, 'c', "[abc]*")
                .build();

        assertThrowsRDE(clazz);
    }

    @Test
    void invalidFillerInvalidRegexPattern() {
        Class<?> clazz = MyByteBuddy.createClass()
                .addRecord()
                .addStandardField()
                .addFiller(1, 2, "[a*")
                .build();

        assertThrowsRDE(clazz);
    }

    @Test
    void name() {
        List<String> list = new BufferedReader(null)
                .lines()
                .filter(((Predicate<String>)String::isEmpty).negate())
                .collect(toList());
    }

}