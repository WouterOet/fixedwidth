package fixedwidth.bytebuddy;

import fixedwidth.annotations.Filler;
import fixedwidth.annotations.Fillers;

import java.lang.annotation.Annotation;

public class FillersImpl implements Fillers {

    private final Filler[] fillers;

    public FillersImpl(Filler[] fillers) {
        this.fillers = fillers;
    }

    @Override
    public Filler[] value() {
        return fillers;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Fillers.class;
    }
}
