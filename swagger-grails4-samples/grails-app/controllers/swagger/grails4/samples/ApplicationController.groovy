package swagger.grails4.samples

import grails.core.GrailsApplication
import grails.plugins.*
import swagger.grails4.openapi.ApiDoc

//@ApiDoc(tag = {
//    description "Application Info"
//})
class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    @ApiDoc(operation = {
        summary "List Application Information"
        description "List Application information，includes controller、plugin、artifacts etc."
    })
    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }
}
