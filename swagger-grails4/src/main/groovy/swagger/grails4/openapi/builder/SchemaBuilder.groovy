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
        schemaMap.each { name, closure ->
            def builder = new SchemaBuilder(reader: reader)
            def schema = evaluateClosure(closure, builder)
            model.properties.put(name, schema)
        }
    }
}
