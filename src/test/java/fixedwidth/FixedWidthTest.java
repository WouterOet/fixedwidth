package fixedwidth;

import fixedwidth.annotations.Converter;
import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedWidthTest {

    @Test
    void forClassNullCheck() {
        assertThrows(NullPointerException.class, () -> FixedWidth.forClass(null));
    }

    @Record
    private static class TestClass {
        @Position(start = 0, end = 1)
        private int i;
    }

    @Test
    void lineLength() {
        assertThrows(ParseException.class, () -> FixedWidth.forClass(TestClass.class).parse(""));
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
}