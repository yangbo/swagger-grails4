package swagger.grails4

import grails.core.GrailsApplication
import grails.web.mapping.LinkGenerator
import grails.web.mapping.UrlMappingsHolder
import io.swagger.v3.oas.integration.GenericOpenApiContext
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiContext
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.ApplicationContext
import swagger.grails4.openapi.GrailsScanner
import swagger.grails4.openapi.Reader

/**
 * 生成 OpenAPI 文档json的服务
 */
class OpenApiService {

    LinkGenerator linkGenerator

    GrailsApplication grailsApplication
    ApplicationContext applicationContext

    /**
     * 生成 OpenAPI 文档对象
     */
    def generateDocument() {
        OpenAPIConfiguration config = new SwaggerConfiguration().openAPI(configOpenApi())
        config.setReaderClass("swagger.grails4.openapi.Reader")

        OpenApiContext ctx = new GenericOpenApiContext().openApiConfiguration(config)
        ctx.setOpenApiScanner(new GrailsScanner(grailsApplication: grailsApplication))
        ctx.setOpenApiReader(new Reader(application: grailsApplication, config: config))
        ctx.init()
        OpenAPI openAPI = ctx.read()
    }

    /**
     * Create an OpenAPI object with configured ahead.
     * @return OpenAPI object has been configured.
     */
    OpenAPI configOpenApi() {
        // TODO resolve config from groovy script or annotation
        new OpenAPI().info(new Info().description("TEST INFO DESC"))
    }
}
