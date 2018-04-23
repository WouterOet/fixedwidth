package fixedwidth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates which subsection of the entire string is applicable for the field this annotation is used on.
 * The @{code end} needs to be higher than @{code start}. These values are used to call @{code String::substring} and
 * will thus will have the same behavior.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Position {

    /**
     * The starting position of the mapping
     */
    int start();

    /**
     * The end position of the mapping
     */
    int end();
}
