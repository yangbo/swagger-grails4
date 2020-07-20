package swagger.grails4.samples

import grails.core.GrailsApplication
import grails.plugins.*
import swagger.grails4.openapi.ApiDoc

@ApiDoc(tag = {
    description "Application Info"
})
class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    @ApiDoc(operation = {
        summary "This is the Sample Application api. 这是一个示例API应用"
    })
    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }
}
