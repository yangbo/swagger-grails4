package swagger.grails4.openapi.builder

import io.swagger.v3.oas.models.media.MediaType

class MediaTypeBuilder implements AnnotationBuilder<MediaType> {
    MediaType model = new MediaType()
    /**
     * needed by AnnotationBuilder trait.
     * This is not an annotation but model class, but it should work as the same.
     */
    @SuppressWarnings("unused")
    static Class openApiAnnotationClass = MediaType

    /**
     * Build schema from class or closure
     * @param classOrClosure domain class or schema definition closure
     * @param options additional options such as:
     *  'properties' - to override some properties of schema.
     */
    def schema(Map options, classOrClosure) {
        SchemaBuilder builder = new SchemaBuilder(reader: reader)
        model.schema = builder.buildSchema(classOrClosure)

        if (options && options["properties"]) {
            // override properties of schema
            options["properties"].each { propName, propDefinition ->
                def propSchemaBuilder = new SchemaBuilder(reader: reader)
                this.model.schema.properties.put(propName, propSchemaBuilder.buildSchema(propDefinition))
            }
        }
    }

    def schema(classOrClosure) {
        schema([:], classOrClosure)
    }

    /**
     * for call in "schema classOrClosure, [properties: {...}]" form
     */
    def schema(classOrClosure, Map options) {
        schema(options, classOrClosure)
    }
}
