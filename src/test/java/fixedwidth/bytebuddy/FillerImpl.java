package fixedwidth.bytebuddy;

import fixedwidth.annotations.Filler;
import fixedwidth.annotations.Position;

import java.lang.annotation.Annotation;

class FillerImpl implements Filler {
    private final int start;
    private final int end;
    private final char c;
    private final String pattern;

    public FillerImpl(int start, int end, char c, String pattern) {
        this.start = start;
        this.end = end;
        this.c = c;
        this.pattern = pattern;
    }

    @Override
    public Position position() {
        return new PositionImpl(start, end);
    }

    @Override
    public char character() {
        return c;
    }

    @Override
    public String regex() {
        return pattern;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Filler.class;
    }
}
