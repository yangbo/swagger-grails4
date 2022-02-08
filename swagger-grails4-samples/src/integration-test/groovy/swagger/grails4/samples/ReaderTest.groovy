package swagger.grails4.samples

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import grails.testing.spring.AutowiredTest
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
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
        def operation = pathMap.value.post
        operation.parameters.size() > 0
        operation.parameters[0].name == "username"
        operation.parameters[1].name == "password"
    }

    def "Extract only one comment block"() {
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        def command = openAPI.components.schemas.get("swagger.grails4.samples.UserCommand")
        then:
        command.properties["onlyOneCommentBlock"].description == "Only One Comment Block Should be extracted."
    }

    def "Extract list type"() {
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        def command = openAPI.components.schemas.get("swagger.grails4.samples.UserCommand")
        then:
        def prop = command.properties["listProperty"]
        prop.type == "array"
        prop.items.type == "string"
    }

    /**
     * Create an OpenAPI object with configured ahead.
     * @return OpenAPI object has been configured.
     */
    OpenAPI configOpenApi() {
        new OpenAPI().info(new Info().description("TEST INFO DESC"))
    }

    def "Test build type"() {
        when:
        byte[] array = new byte[0]
        def list = []
        def map = [:]
        then:
        Reader.buildType(int).type == "integer"
        Reader.buildType(String).type == "string"
        Reader.buildType(list.class).type == "array"
        Reader.buildType(array.class).type == "array"
        Reader.buildType(map.getClass()).type == "object"
        Reader.buildType(UserCommand).type == "object"
    }

    def "Test Date class"() {
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        def command = openAPI.components.schemas.get("swagger.grails4.samples.UserCommand")
        then:
        def starTime = command.properties["startTime"]
        starTime.type == "string"
        starTime.format == "date-time"
    }

    def "Response schema should support comments-to-description"() {
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        def response = openAPI.components.schemas.get("swagger.grails4.samples.RestApiResponse")
        println(response)
        then:
        response.properties["code"].description
        response.properties["msg"].description
        response.properties["info"].description
    }

    def "response schema with overridden 'properties'"() {
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        PathItem pathItem = openAPI.paths.get("/user/login")
        def schema = pathItem.post.responses.get("200").content.get("default").schema
        println(schema)
        then:
        // schema with properties-overridden should not use $ref
        !schema.$ref
        schema.properties["info"].name == UserCommand.name
    }

    def "multiple trait properties"(){
        when:
        OpenAPI openAPI = reader.read([UserController] as Set, [:])
        def userCommand = openAPI.components.schemas.get("swagger.grails4.samples.UserCommand")
        println(userCommand)
        then:
        userCommand.properties["offset"]
        //userCommand.properties["offset"].description
        userCommand.properties["max"]
        //userCommand.properties["max"].description
    }
}
