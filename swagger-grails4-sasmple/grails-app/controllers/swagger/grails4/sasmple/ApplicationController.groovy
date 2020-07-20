package swagger.grails4.sasmple

import grails.core.GrailsApplication
import grails.plugins.*
import swagger.grails4.openapi.ApiDoc

class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    @ApiDoc(operation = {
        summary "This is the Application information api."
    })
    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }
}
