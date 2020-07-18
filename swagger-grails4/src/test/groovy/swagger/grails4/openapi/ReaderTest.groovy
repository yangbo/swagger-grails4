package swagger.grails4.openapi

import grails.testing.web.controllers.ControllerUnitTest
import io.swagger.v3.oas.integration.GenericOpenApiContext
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiContext
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import spock.lang.Specification
import swagger.grails4.OpenApiController

class ReaderTest extends Specification implements ControllerUnitTest<OpenApiController> {
    def "Test Read"() {
        when:
        OpenAPIConfiguration config = new SwaggerConfiguration().openAPI(configOpenApi())
        config.setReaderClass("swagger.grails4.openapi.Reader")

        OpenApiContext ctx = new GenericOpenApiContext().openApiConfiguration(config)
        ctx.setOpenApiScanner(new GrailsScanner(grailsApplication: grailsApplication))
        ctx.init()
        ctx.getOpenApiReader().application = grailsApplication
        OpenAPI openAPI = ctx.read()
        then:
        openAPI
    }

    /**
     * Create an OpenAPI object with configured ahead.
     * @return OpenAPI object has been configured.
     */
    OpenAPI configOpenApi() {
        new OpenAPI().info(new Info().description("TEST INFO DESC"))
    }
}
