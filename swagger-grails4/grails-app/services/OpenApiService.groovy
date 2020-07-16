import grails.boot.GrailsApp
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.web.mapping.LinkGenerator

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
        for (GrailsClass cls in grailsApplication.controllerClasses) {
            println cls
        }
    }
}
