package fixedwidth.bytebuddy;

import fixedwidth.annotations.Position;

import java.lang.annotation.Annotation;

class PositionImpl implements Position {

    private final int start;
    private final int end;

    public PositionImpl(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int start() {
        return start;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Position.class;
    }
}
