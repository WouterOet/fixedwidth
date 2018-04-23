package fixedwidth;

import fixedwidth.annotations.Record;

import java.util.Collections;
import java.util.List;

class ScanResult {

    private final List<Mapping> mappings;
    private final List<ParsedFiller> fillers;
    private final Record record;

    ScanResult(List<Mapping> mappings, List<ParsedFiller> fillers, Record record) {
        this.mappings = Collections.unmodifiableList(mappings);
        this.fillers = Collections.unmodifiableList(fillers);
        this.record = record;
    }

    List<Mapping> getMappings() {
        return mappings;
    }

    List<ParsedFiller> getFillers() {
        return fillers;
    }

    Record getRecord() {
        return record;
    }
}
