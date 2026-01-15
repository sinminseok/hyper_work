package hyper.run.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributionLock {

    String key();

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long waitTime();

    long leaseTime();

    String prefix() default "";

    boolean useTransaction() default true;
}
