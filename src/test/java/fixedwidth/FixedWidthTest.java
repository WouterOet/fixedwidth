package fixedwidth;

import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;
import org.junit.jupiter.api.Test;

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
}