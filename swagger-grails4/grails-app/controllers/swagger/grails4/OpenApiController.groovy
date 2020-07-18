package swagger.grails4

import grails.web.mime.MimeType
import io.swagger.v3.core.util.Json
import swagger.grails4.openapi.ApiDoc

/**
 * OpenAPI v3 api document controller
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
@ApiDoc(tag={
    name "OpenAPI v3 controller"
    description "The OpenAPI v3 api document controller"
})
class OpenApiController {

    OpenApiService openApiService

    @ApiDoc(operation={
        summary "The Index of OpenAPI documents"
    })
    def index() {
        def doc = openApiService.generateDocument()
        def json = Json.pretty().writeValueAsString(doc)
        render(contentType: MimeType.JSON, json)
    }
}
