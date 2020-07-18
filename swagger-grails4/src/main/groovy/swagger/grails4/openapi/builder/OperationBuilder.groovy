package swagger.grails4.openapi.builder

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation

/**
 * Operation model builder.
 *
 * Delegate object is OpenAPI.
 *
 * @see io.swagger.v3.oas.annotations.Operation
 */
class OperationBuilder implements AnnotationBuilder {

    Operation model = new Operation()
    static Class openApiAnnotationClass = io.swagger.v3.oas.annotations.Operation
    /**
     * The annotation elements need specific process so they should be skipped from directly assign.
     * Used when there is not such annotation elements in property in models.
     */
    // List overrideElements = ["method"]

    OperationBuilder(){
        annotationToProperties()
    }

    /**
     * Construct Paths/PathItem
     *
     * @param methodName
     */
    def method(String methodName) {

    }
}
