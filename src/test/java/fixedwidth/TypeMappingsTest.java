package fixedwidth;

import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeMappingsTest {

    @Test
    void test() {
        FixedWidth<TestClass> fixedWidth = FixedWidth.forClass(TestClass.class);

        TestClass actual = fixedWidth.parse("11 13 14 123jk 1988-11-26T20:08:01 3.1415 3.1415");
        TestClass expected = new TestClass(
                11,
                11,
                13L,
                13L,
                (short) 14,
                (short) 14,
                "123jk",
                LocalDate.parse("1988-11-26"),
                LocalDateTime.parse("1988-11-26T20:08:01"),
                3.1415,
                3.1415,
                3.1415f,
                3.1415f
        );

        assertEquals(expected, actual);
    }

    @Record
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
        private LocalDate someLocalDate;
        @Position(start = 15, end = 34)
        private LocalDateTime someLocalDateTime;
        @Position(start = 35, end = 41)
        private double someDouble;
        @Position(start = 35, end = 41)
        private Double someDoubleBoxed;
        @Position(start = 42, end = 48)
        private float someFloat;
        @Position(start = 42, end = 48)
        private Float someFloatBoxed;


        public TestClass() {
        }

        public TestClass(int someInt, Integer someInteger, long someLong, Long someLongBoxed, short someShort, Short someShortBoxed, String someString, LocalDate someLocalDate, LocalDateTime someLocalDateTime, double someDouble, Double someDoubleBoxed, float someFloat, Float someFloatBoxed) {
            this.someInt = someInt;
            this.someInteger = someInteger;
            this.someLong = someLong;
            this.someLongBoxed = someLongBoxed;
            this.someShort = someShort;
            this.someShortBoxed = someShortBoxed;
            this.someString = someString;
            this.someLocalDate = someLocalDate;
            this.someLocalDateTime = someLocalDateTime;
            this.someDouble = someDouble;
            this.someDoubleBoxed = someDoubleBoxed;
            this.someFloat = someFloat;
            this.someFloatBoxed = someFloatBoxed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return someInt == testClass.someInt &&
                    someLong == testClass.someLong &&
                    someShort == testClass.someShort &&
                    Double.compare(testClass.someDouble, someDouble) == 0 &&
                    Float.compare(testClass.someFloat, someFloat) == 0 &&
                    Objects.equals(someInteger, testClass.someInteger) &&
                    Objects.equals(someLongBoxed, testClass.someLongBoxed) &&
                    Objects.equals(someShortBoxed, testClass.someShortBoxed) &&
                    Objects.equals(someString, testClass.someString) &&
                    Objects.equals(someLocalDate, testClass.someLocalDate) &&
                    Objects.equals(someLocalDateTime, testClass.someLocalDateTime) &&
                    Objects.equals(someDoubleBoxed, testClass.someDoubleBoxed) &&
                    Objects.equals(someFloatBoxed, testClass.someFloatBoxed);
        }

        @Override
        public int hashCode() {

            return Objects.hash(someInt, someInteger, someLong, someLongBoxed, someShort, someShortBoxed, someString, someLocalDate, someLocalDateTime, someDouble, someDoubleBoxed, someFloat, someFloatBoxed);
        }
    }
}

