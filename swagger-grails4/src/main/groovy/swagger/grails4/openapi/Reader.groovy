package swagger.grails4.openapi


import grails.core.GrailsApplication
import grails.core.GrailsControllerClass
import grails.web.mapping.UrlMappingsHolder
import groovy.util.logging.Slf4j
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiReader
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.ApplicationContext
import swagger.grails4.openapi.builder.OperationBuilder

import java.lang.reflect.Method


/**
 * Groovy annotation reader for OpenAPI
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
@Slf4j
class Reader implements OpenApiReader {
    public static final String DEFAULT_MEDIA_TYPE_VALUE = "*/*"
    public static final String DEFAULT_DESCRIPTION = "default response"

    OpenAPIConfiguration config
    GrailsApplication application
    ApplicationContext applicationContext

    private OpenAPI openAPI = new OpenAPI()
    private Components components = new Components()
    private Paths paths = new Paths()
    private Set<Tag> openApiTags = new LinkedHashSet<>()

    private static final String GET_METHOD = "get";
    private static final String POST_METHOD = "post";
    private static final String PUT_METHOD = "put";
    private static final String DELETE_METHOD = "delete";
    private static final String PATCH_METHOD = "patch";
    private static final String TRACE_METHOD = "trace";
    private static final String HEAD_METHOD = "head";
    private static final String OPTIONS_METHOD = "options";

    @Override
    void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        this.config = openApiConfiguration
    }

    /**
     * Read controller classes, build OpenAPI object.
     *
     * @param classes controller classes or any classes with @ApiDoc annotation
     * @param resources TODO Understanding what it is
     * @return openAPI object
     */
    @Override
    OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        classes.each {
            processApiDocAnnotation(it)
        }
        openAPI
    }

    def processApiDocAnnotation(Class controllerClass) {
        log.debug("Scanning class: ${controllerClass.simpleName}")
        // get all controller grails artifacts
        def allControllerArtifacts = application.getArtefacts("Controller")
        // find controller artifact with the same controller class
        GrailsControllerClass controllerArtifact = allControllerArtifacts.find { it.clazz == controllerClass } as GrailsControllerClass
        if (!controllerArtifact) {
            log.error("No grails controller found for class ${controllerClass}")
            return
        }
        def urlMappingsHolder = applicationContext.getBean("grailsUrlMappingsHolder", UrlMappingsHolder)
        urlMappingsHolder.urlMappings.each {
            log.debug("url mapping: ${it}")
        }
        // iterate actions only
        controllerArtifact.actions.each { String actionName ->
            log.debug("Scanning action: ${actionName}")
            // get java reflection method object
            Method method = controllerClass.methods.find { it.name == actionName }
            if (!method) {
                log.error("No method found for action '${actionName}'!")
                return
            }
            def apiDoc = method.getAnnotation(ApiDoc)
            if (!apiDoc) {
                return
            }
            // get url from controller and action

            // process operation closure
            // call the constructor of Closure(Object owner, Object thisObject)
            def closureClass = apiDoc.operation()
            if (closureClass) {
                Closure operation = closureClass.newInstance(openAPI, openAPI)
                def operationBuilder = new OperationBuilder(openAPI: openAPI)
                operation.delegate = operationBuilder
                operation.resolveStrategy = Closure.DELEGATE_FIRST
                operation()
            }
        }
    }

    def processAction() {

    }
}
