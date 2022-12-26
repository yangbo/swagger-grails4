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

    /**
     * Build schema from class or closure
     * @param classOrClosure domain class or schema definition closure
     */
    def schema(classOrClosure) {
        SchemaBuilder builder = new SchemaBuilder(reader: reader)
        model.schema = builder.buildSchema(classOrClosure)
    }
}
