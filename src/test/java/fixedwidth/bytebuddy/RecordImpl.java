package fixedwidth.bytebuddy;

import fixedwidth.annotations.Record;

import java.lang.annotation.Annotation;

class RecordImpl implements Record {

    private final boolean allowOverlapping;
    private final boolean allowUnmapped;

    public RecordImpl(boolean allowOverlapping, boolean allowUnmapped) {
        this.allowOverlapping = allowOverlapping;
        this.allowUnmapped = allowUnmapped;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Record.class;
    }

    @Override
    public boolean allowOverlapping() {
        return allowOverlapping;
    }

    @Override
    public boolean allowUnmapped() {
        return allowUnmapped;
    }
}