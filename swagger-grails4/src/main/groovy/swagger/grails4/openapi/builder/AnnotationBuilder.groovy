package swagger.grails4.openapi.builder


import java.lang.reflect.Method

/**
 * Provide some utility methods for annotation closure builders.
 * Such as methods to extract properties from OpenAPI annotation.
 * <pre>
 * Require implementing class has below properties:
 * - "openApiAnnotation" property, related OpenAPI annotation class.
 * - "model" property, the OpenAPI model object.
 * </pre>
 * Require implementing class call initPrimitiveElements() in their constructor.
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
trait AnnotationBuilder<T> {
    /**
     * The builder's production, should be override by implementing class
     */
    T model
    /**
     * The reader that use these builders
     */
    swagger.grails4.openapi.Reader reader

    private List systemMethods = ["equals", "toString", "hashCode", "annotationType"]

    /**
     * The annotation elements that can be assigned directly to model, it has the same method name in closure.
     */
    private Set primitiveElements = []

    /**
     * Extract properties from OpenAPI annotation
     */
    def initPrimitiveElements() {
        if (openApiAnnotationClass instanceof Closure) {
            initAnnotationPrimitiveElements()
        }else{
            initClassPrimitiveProperties()
        }
    }

    def initClassPrimitiveProperties(){
        openApiAnnotationClass.metaClass.properties.each { MetaProperty metaProperty ->
            if (isPrimitiveElement(metaProperty.type)){
                primitiveElements << metaProperty.name
            }
        }
    }

    def initAnnotationPrimitiveElements() {
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
                // must use getModel() instead of 'this.model' because we need implementing class overridden property
                if (getModel().hasProperty(propertyName)) {
                    getModel()[propertyName] = method.defaultValue
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
    def methodMissing(String name, Object args) {
        if (!primitiveElements) {
            initPrimitiveElements()
        }
        // assign primitive element value to model directly
        if (name in primitiveElements) {
            getModel()[name] = args[0]
        }
    }

    /**
     * Evaluate closure by delegating to builder and return the production model.
     * @param closure api doc closure
     * @param builder annotation builder
     * @return the builder's model
     */
    def <M> M evaluateClosure(Closure closure, AnnotationBuilder<M> builder) {
        def builderClosure = closure.rehydrate(builder, this, this)
        builderClosure.resolveStrategy = Closure.DELEGATE_ONLY
        builderClosure()
        builder.model
    }
}
