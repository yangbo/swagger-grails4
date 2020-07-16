package swagger.grails4.openapi

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * The OpenAPI annotation in Groovy Closure style.
 */
@Retention(RetentionPolicy.RUNTIME)
@interface OpenApi {
    /**
     * the closure of api document
     */
    Class value()
    /**
     * annotation is in controller or action method, true in controller, false in action method
     */
    boolean controller() default false
}
