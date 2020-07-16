package swagger.grails4.openapi

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * The global configuration for OpenAPI document
 */
@Retention(RetentionPolicy.RUNTIME)
@interface OpenApiConfig {
    /**
     * The global OpenAPI config closure, it can only be add in one class for one application.
     */
    Class value()
}
