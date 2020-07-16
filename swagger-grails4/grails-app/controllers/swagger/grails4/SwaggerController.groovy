package swagger.grails4

import io.swagger.v3.oas.annotations.Operation

class SwaggerController {

    OpenApiService openApiService

    @Operation(summary = "swagger文档入口")
    def index() {
        def doc = openApiService.generateDocument()
        render(doc)
    }
}
