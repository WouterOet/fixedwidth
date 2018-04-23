package fixedwidth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class can be parsed by fixedwidth. The annotated class needs to have a default
 * constructor and cannot be a non=static inner class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Record {

    boolean allowOverlapping() default false;

    boolean allowUnmapped() default false;
}
