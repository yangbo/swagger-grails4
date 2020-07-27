package swagger.grails4.openapi.builder

import io.swagger.v3.oas.models.media.Schema

class SchemaBuilder implements AnnotationBuilder<Schema> {
    Schema model = new Schema()

    /**
     * needed by AnnotationBuilder trait
     */
    @SuppressWarnings("unused")
    static Class openApiAnnotationClass = Schema

    def properties(Map<String, Closure> schemaMap) {
        if (!model.properties) {
            model.properties = [:]
        }
        schemaMap.each { name, classOrClosure ->
            def schema = buildSchema(classOrClosure)
            model.properties.put(name, schema)
        }
    }

    /**
     * Build Schema object from class or closure.
     */
    Schema buildSchema(Object classOrClosure) {
        if (classOrClosure instanceof Closure) {
            def builder = new SchemaBuilder(reader: reader)
            return evaluateClosure(classOrClosure, builder)
        }else{
            return reader.buildSchema(classOrClosure as Class)
        }
    }
}
