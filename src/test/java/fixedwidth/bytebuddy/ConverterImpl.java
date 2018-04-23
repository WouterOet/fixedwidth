package fixedwidth.bytebuddy;

import fixedwidth.annotations.Converter;

import java.lang.annotation.Annotation;

public class ConverterImpl implements Converter {

    private final Class<?> converter;

    public ConverterImpl(Class<?> converter) {
        this.converter = converter;
    }

    @Override
    public Class<?> value() {
        return converter;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Converter.class;
    }
}
