package swagger.grails4

import grails.util.Environment
import io.swagger.v3.core.util.Json
import swagger.grails4.openapi.ApiDoc

/**
 * OpenAPI v3 api document controller
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
@ApiDoc(tag = {
    description "The OpenAPI v3 api document controller"
})
class OpenApiController {

    OpenApiService openApiService

    @ApiDoc(operation = {
        summary "OpenApi json documents"
        description "The OpenAPI API v3 json/yaml documents"
        responses "200": {
            content "application/json": {
                description "Swagger documentation"
            }
        }
    })
    def document() {
        def doc = [:]
        if (Environment.current != Environment.PRODUCTION){
            doc = openApiService.generateDocument()
        }
        def json = Json.pretty().writeValueAsString(doc)
        render(text: json, contentType: "application/json", encoding: "UTF-8")
    }

    /**
     * Redirect to /static/api/doc.html
     */
    def index() {
        render view: '/openApi/index'
    }
}
