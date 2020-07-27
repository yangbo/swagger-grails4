package swagger.grails4.openapi.builder

import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.responses.ApiResponse

/**
 * Build Response object of OASv3.
 */
class ResponseBuilder implements AnnotationBuilder<ApiResponse> {
    ApiResponse model = new ApiResponse()

    /**
     * needed by AnnotationBuilder trait
     */
    @SuppressWarnings("unused")
    static Class openApiAnnotationClass = io.swagger.v3.oas.annotations.responses.ApiResponse

    ResponseBuilder() {
        initPrimitiveElements()
    }

    /**
     * Build Content object like "content 'application/json': {...} "
     * @param closure content config closure, delegate to ContentBuilder
     */
    def content(Map<String, Closure> closureMap) {
        if (!model.content) {
            model.content = new Content()
        }
        closureMap.each { mime, closure ->
            MediaTypeBuilder mediaTypeBuilder = new MediaTypeBuilder(reader: reader)
            model.content.addMediaType(mime, evaluateClosure(closure, mediaTypeBuilder))
        }
    }
}
