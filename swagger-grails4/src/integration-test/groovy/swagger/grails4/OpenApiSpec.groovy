package swagger.grails4

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import grails.testing.spring.AutowiredTest
import grails.web.mapping.UrlMappingsHolder
import io.swagger.v3.oas.integration.GenericOpenApiContext
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiContext
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import swagger.grails4.openapi.GrailsScanner
import swagger.grails4.openapi.Reader

@Integration
class OpenApiSpec extends Specification implements AutowiredTest {

    GrailsApplication grailsApplication

    def setup() {
    }

    def cleanup() {
    }

    def "Test Read"() {
        when:
        OpenAPIConfiguration config = new SwaggerConfiguration().openAPI(configOpenApi())
        config.setReaderClass("swagger.grails4.openapi.Reader")

        OpenApiContext ctx = new GenericOpenApiContext().openApiConfiguration(config)
        ctx.setOpenApiScanner(new GrailsScanner(grailsApplication: grailsApplication))
        ctx.setOpenApiReader(new Reader(application: grailsApplication, config: config, applicationContext: applicationContext))
        ctx.init()
        OpenAPI openAPI = ctx.read()
        then:
        println (openAPI)
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
