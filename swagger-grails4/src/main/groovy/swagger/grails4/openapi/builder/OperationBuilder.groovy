package swagger.grails4.openapi.builder


import io.swagger.v3.oas.models.Operation

/**
 * Operation model builder.
 *
 * Delegate object is OpenAPI.
 *
 * @see io.swagger.v3.oas.annotations.Operation
 * @author bo.yang <bo.yang@telecwin.com>
 */
class OperationBuilder implements AnnotationBuilder {

    Operation model = new Operation()
    /**
     * needed by AnnotationBuilder trait
     */
    @SuppressWarnings("unused")
    static Class openApiAnnotationClass = io.swagger.v3.oas.annotations.Operation

    OperationBuilder(){
        initPrimitiveElements()
    }
}
