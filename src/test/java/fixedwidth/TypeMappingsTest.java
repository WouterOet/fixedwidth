package fixedwidth;

import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;
import fixedwidth.annotations.Temporal;
import fixedwidth.annotations.WithPattern;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TypeMappingsTest {

    @Test
    void test() {
        FixedWidth<TestClass> fixedWidth = FixedWidth.forClass(TestClass.class);

        TestClass actual = fixedWidth.parse("11 13 14 123jk 1988-11-26T20:08:01 3.1415 3.1415 A true 125 Q");

        assertAll(
                () -> assertEquals(11, actual.someInt),
                () -> assertEquals(Integer.valueOf(11), actual.someInteger),
                () -> assertEquals(13, actual.someLong),
                () -> assertEquals(Long.valueOf(13L), actual.someLongBoxed),
                () -> assertEquals((short) 14, actual.someShort),
                () -> assertEquals(Short.valueOf((short) 14), actual.someShortBoxed),
                () -> assertEquals("123jk", actual.someString),
                () -> assertEquals(LocalDate.parse("1988-11-26"), actual.someLocalDate),
                () -> assertEquals(LocalDateTime.parse("1988-11-26T20:08:01"), actual.someLocalDateTime),
                () -> assertEquals(3.1415, actual.someDouble),
                () -> assertEquals(Double.valueOf(3.1415), actual.someDoubleBoxed),
                () -> assertEquals(3.1415f, actual.someFloat),
                () -> assertEquals(Float.valueOf(3.1415f), actual.someFloatBoxed),
                () -> assertEquals(TestEnum.A, actual.someEnum),
                () -> assertTrue(actual.someBoolean),
                () -> assertEquals(Boolean.TRUE, actual.someBooleanBoxed),
                () -> assertEquals((byte) 125, actual.someByte),
                () -> assertEquals(Byte.valueOf((byte) 125), actual.someByteBoxed)
        );
    }

    enum TestEnum {
        A, B, C
    }

    @Record(allowUnmapped = true, allowOverlapping = true)
    private static class TestClass {

        @Position(start = 0, end = 2)
        private int someInt;
        @Position(start = 0, end = 2)
        private Integer someInteger;
        @Position(start = 3, end = 5)
        private long someLong;
        @Position(start = 3, end = 5)
        private Long someLongBoxed;
        @Position(start = 6, end = 8)
        private short someShort;
        @Position(start = 6, end = 8)
        private Short someShortBoxed;
        @Position(start = 9, end = 14)
        private String someString;
        @Position(start = 15, end = 25)
        @WithPattern(pattern = "yyyy-MM-dd", temporal = Temporal.DATE)
        private LocalDate someLocalDate;
//        @Position(start = 15, end = 25)
//        @WithPattern(pattern = "yyyy-MM-dd", temporal = WithPattern.Temporal.DATE)
//        private Date someDate;
        @Position(start = 15, end = 34)
        @WithPattern(pattern = "yyyy-MM-dd'T'HH:mm:ss", temporal = Temporal.DATE_TIME)
        private LocalDateTime someLocalDateTime;
//        @Position(start = 15, end = 34)
//        @WithPattern(pattern = "yyyy-MM-dd'T'HH:mm:ss", temporal = WithPattern.Temporal.DATE_TIME)
//        private Date someDateTime;
        @Position(start = 35, end = 41)
        private double someDouble;
        @Position(start = 35, end = 41)
        private Double someDoubleBoxed;
        @Position(start = 42, end = 48)
        private float someFloat;
        @Position(start = 42, end = 48)
        private Float someFloatBoxed;
        @Position(start = 49, end = 50)
        private TestEnum someEnum;
        @Position(start = 51, end = 55)
        private boolean someBoolean;
        @Position(start = 51, end = 55)
        private Boolean someBooleanBoxed;
        @Position(start = 56, end = 59)
        private byte someByte;
        @Position(start = 56, end = 59)
        private Byte someByteBoxed;
        @Position(start = 60, end = 61)
        private char someChar;
        @Position(start = 60, end = 61)
        private Character someCharBoxed;

    }
}

