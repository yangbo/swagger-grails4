package swagger.grails4.openapi.builder

import io.swagger.v3.oas.models.tags.Tag

class TagBuilder implements AnnotationBuilder {
    Tag model = new Tag()
    /**
     * needed by AnnotationBuilder trait
     */
    @SuppressWarnings("unused")
    static Class openApiAnnotationClass = io.swagger.v3.oas.annotations.tags.Tag

    TagBuilder(){
        initPrimitiveElements()
    }
}
