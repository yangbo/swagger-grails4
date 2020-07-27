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
class OperationBuilder implements AnnotationBuilder<Operation> {

    Operation model = new Operation()
    /**
     * needed by AnnotationBuilder trait
     */
    @SuppressWarnings("unused")
    static Class openApiAnnotationClass = io.swagger.v3.oas.annotations.Operation

    OperationBuilder(){
        initPrimitiveElements()
    }

    /**
     * The "parameters" member of @ApiDoc.
     * @param parameterClosures closure of parameters, delegate to ParameterBuilder.
     * @return void
     */
    def parameters(List<Closure> parameterClosures) {
        if (!model.parameters) {
            model.parameters = []
        }
        parameterClosures.each { closure ->
            ParameterBuilder builder = new ParameterBuilder(reader: reader)
            model.parameters << evaluateClosure(closure, builder)
        }
    }

    def responses(Map<String, Closure> responsesClosures) {
        if (!model.responses) {
            model.responses = []
        }
        responsesClosures.each { code, closure ->
            ResponseBuilder builder = new ResponseBuilder(reader: reader)
            def resp = evaluateClosure(closure, builder)
            model.responses.put(code, resp)
        }
    }
}
