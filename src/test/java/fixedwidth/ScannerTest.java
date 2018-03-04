package fixedwidth;

import fixedwidth.annotations.Position;
import fixedwidth.annotations.Record;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScannerTest {

    // TODO verify messages
    private void scan(Class<?> clazz, String message) {
        Executable r = () -> new Scanner(clazz).scan();
        RecordDefinitionException exception = assertThrows(RecordDefinitionException.class, r);

        assertEquals(message, exception.getMessage());
    }

    private static class MissingRecordAnnotation {
    }

    @Test
    void missingRecordAnnotation() {
        scan(MissingRecordAnnotation.class, "The class fixedwidth.ScannerTest$MissingRecordAnnotation is missing the fixedwidth.annotions.Record annotation");
    }

    @Record
    private static class MissingPositionAnnotation {
        private int i;
    }


    @Test
    void missingPosition() {
        scan(MissingPositionAnnotation.class,
                "The field i on class fixedwidth.ScannerTest$MissingPositionAnnotation is missing the annotation fixedwidth.annotations.Position");
    }

    @Record
    private static class InvalidPositionSameValue {
        @Position(start = 0, end = 0)
        private int i;
    }


    @Test
    void invalidPositionSameValue() {
        scan(InvalidPositionSameValue.class,
                "The field i on class fixedwidth.ScannerTest$InvalidPositionSameValue has an invalid fixedwidth.annotations.Position combination. The end needs to be after the start");
    }

    @Record
    private static class InvalidPositionStartAfterEnd {
        @Position(start = 1, end = 0)
        private int i;
    }


    @Test
    void invalidPositionStartAfterEnd() {
        scan(InvalidPositionStartAfterEnd.class,
                "The field i on class fixedwidth.ScannerTest$InvalidPositionStartAfterEnd has an invalid fixedwidth.annotations.Position combination. The end needs to be after the start");
    }

    @Record
    private static class MissingFields {}

    @Test
    void missingFields() {
        scan(MissingFields.class,
                "The class fixedwidth.ScannerTest$MissingFields has no mappable fields");
    }
}