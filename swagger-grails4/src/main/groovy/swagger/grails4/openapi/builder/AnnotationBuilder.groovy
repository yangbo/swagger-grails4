package swagger.grails4.openapi.builder

import io.swagger.v3.oas.models.OpenAPI

import java.lang.reflect.Method

/**
 * Extract properties from OpenAPI annotation.
 * <pre>
 * Require implementing class has below properties:
 * - "openApiAnnotation" property, related OpenAPI annotation class.
 * - "model" property, the OpenAPI model object.
 * - "overrideElements" property, contains annotation elements that need additional processing
 *   so they should be skipped from directly assigning and processed by a builder method.
 *   Used when there is no such annotation elements in property of models.
 * </pre>
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
trait AnnotationBuilder {

    /**
     * The OpenAPI object to build
     */
    OpenAPI openAPI

    private List systemMethods = ["equals", "toString", "hashCode", "annotationType"]

    /**
     * The annotation elements that can be assigned directly to model, it has the same method name in closure.
     */
    private Set primitiveElements = []

    /**
     * Extract properties from OpenAPI annotation
     */
    def initPrimitiveElements() {
        openApiAnnotationClass.methods.each { Method method ->
            if (method.name in systemMethods) {
                return
            }
            // override elements to call builder methods
            // if (method.name in this.overrideElements) {
            //     return
            // }
            def elementType = method.returnType
            // add dynamic properties
            String propertyName = method.name
            if (isPrimitiveElement(elementType)) {
                // assign to model directly
                if (this.model.hasProperty(propertyName)) {
                    this.model[propertyName] = method.defaultValue
                    // add to primitive
                    primitiveElements << propertyName
                }
            }
        }
    }

    def isPrimitiveElement(elementType) {
        switch (elementType) {
            case String:
            case String[]:
            case Number:
            case Number[]:
            case Boolean:
            case Boolean[]:
                return true
        }
        return false
    }

    /**
     * For string and string[] annotation attributes we will assign the value to model property.
     *
     * @param name method name
     * @param args method arguments
     */
    def methodMissing(String name, args) {
        // assign primitive element value to model directly
        if (name in primitiveElements) {
            this.model[name] = args[0]
        }
    }
}
