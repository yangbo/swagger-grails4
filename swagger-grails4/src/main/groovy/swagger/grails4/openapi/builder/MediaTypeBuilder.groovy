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
     * @param classOrClosure
     */
    def schema(classOrClosure) {
        if (classOrClosure instanceof Closure) {

        }else{
            model.schema = reader.buildSchema(classOrClosure as Class)
        }
    }
}
