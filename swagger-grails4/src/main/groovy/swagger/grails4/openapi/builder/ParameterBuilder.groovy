package swagger.grails4.openapi.builder

import io.swagger.v3.oas.models.parameters.Parameter

class ParameterBuilder implements AnnotationBuilder<Parameter> {
    Parameter model = new Parameter()
    /**
     * needed by AnnotationBuilder trait
     */
    @SuppressWarnings("unused")
    static Class openApiAnnotationClass = io.swagger.v3.oas.annotations.Parameter

    ParameterBuilder(){
        initPrimitiveElements()
    }

    def inType(String inType) {
        model.in(inType)
    }
}
