package swagger.grails4.samples

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import grails.testing.spring.AutowiredTest
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import spock.lang.Specification
import swagger.grails4.openapi.Reader

@Integration
class ReaderTest extends Specification implements AutowiredTest {
    GrailsApplication grailsApplication

    Reader reader

    def setup() {
        reader = new Reader(
                config: new SwaggerConfiguration().openAPI(configOpenApi()),
                application: grailsApplication)
    }

    def "Test Paths"() {
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        println openAPI
        then:
        openAPI.paths.size() > 0
    }

    def "Test Parameters"() {
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        println openAPI
        then:
        def pathMap = openAPI.paths.find {
            it.value.post?.summary == "Login"
        }
        pathMap.value.parameters.size() > 0
        pathMap.value.parameters[0].name == "username"
        pathMap.value.parameters[1].name == "password"
    }

    /**
     * Create an OpenAPI object with configured ahead.
     * @return OpenAPI object has been configured.
     */
    OpenAPI configOpenApi() {
        new OpenAPI().info(new Info().description("TEST INFO DESC"))
    }
}
