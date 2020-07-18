package swagger.grails4

import grails.core.GrailsApplication
import grails.web.mapping.LinkGenerator
import io.swagger.v3.oas.integration.GenericOpenApiContext
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiContext
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import swagger.grails4.openapi.GrailsScanner

/**
 * 生成 OpenAPI 文档json的服务
 */
class OpenApiService {

    LinkGenerator linkGenerator

    GrailsApplication grailsApplication

    /**
     * 生成 OpenAPI 文档对象
     */
    def generateDocument() {

        OpenAPIConfiguration config = new SwaggerConfiguration().openAPI(configOpenApi());

        OpenApiContext ctx = new GenericOpenApiContext().openApiConfiguration(config)
        ctx.setOpenApiScanner(new GrailsScanner(grailsApplication: grailsApplication))
        ctx.init()

        OpenAPI openApi = ctx.read();
        openApi
    }

    OpenAPI configOpenApi() {
        // TODO resolve config from groovy script or annotation
        new OpenAPI().info(new Info().description("TEST INFO DESC"))
    }
}
