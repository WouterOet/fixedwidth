package fixedwidth.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Fillers.class)
public @interface Filler {

    Position position();

    char character() default '\u0000';

    String regex() default "";

}
