package swagger.grails4.samples

import grails.core.GrailsApplication
import grails.plugins.*
import swagger.grails4.openapi.ApiDoc

class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    @ApiDoc(operation = {
        summary "This is the Sample Application api."
    })
    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }
}
