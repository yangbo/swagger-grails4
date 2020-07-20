package swagger.grails4.openapi

import grails.core.GrailsApplication
import grails.core.GrailsControllerClass
import grails.web.mapping.UrlCreator
import grails.web.mapping.UrlMappingsHolder
import groovy.util.logging.Slf4j
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiReader
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.tags.Tag
import swagger.grails4.openapi.builder.AnnotationBuilder
import swagger.grails4.openapi.builder.OperationBuilder
import swagger.grails4.openapi.builder.TagBuilder

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
        def applicationContext = application.mainContext
        def urlMappingsHolder = applicationContext.getBean("grailsUrlMappingsHolder", UrlMappingsHolder)
        urlMappingsHolder.urlMappings.each {
            log.debug("url mapping: ${it}")
        }
        if (!openAPI.paths) {
            openAPI.paths(new Paths())
        }
        // Add global tags
        Tag controllerTag = buildControllerDoc(controllerArtifact)

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
            // process operation closure
            def closureClass = apiDoc.operation()
            def operationBuilder = new OperationBuilder(openAPI: openAPI)
            // build parameters into model
            buildParameters(operationBuilder.model, method, controllerClass)
            // process operation closure that can override parameters information
            def operation = processClosure(closureClass, operationBuilder) as Operation
            operation.addTagsItem(controllerTag.name)
            buildPathItem(operation, actionName, controllerArtifact, urlMappingsHolder)
        }
    }

    def buildPathItem(Operation operation, String actionName, GrailsControllerClass controllerArtifact, UrlMappingsHolder urlMappingsHolder) {
        // Resolve http method, url from:
        // 1. UrlMapping rule
        // 2. Controller allowedMethods map
        // 3. default as GET

        // 1. from UrlMapping
        def urlMappingOfAction = urlMappingsHolder.urlMappings.find {
            it.controllerName == controllerArtifact.logicalPropertyName && it.actionName == actionName
        }
        PathItem.HttpMethod httpMethod = PathItem.HttpMethod.GET
        String url = ""
        if (urlMappingOfAction) {
            String httpMethodName = urlMappingOfAction.httpMethod.toUpperCase()
            // http method of grails url-mapping rule is '*' or not in PathItem.HttpMethod enum
            // then we use GET method
            if (httpMethodName == "*" || !PathItem.HttpMethod.values()
                    .collect { it.name() }.contains(httpMethodName)) {
                httpMethodName = "GET"
            }
            httpMethod = PathItem.HttpMethod.valueOf(httpMethodName)
            url = urlMappingOfAction.urlData.urlPattern
        } else {
            // 2. from controller
            def allowedMethods = controllerArtifact.getPropertyValue("allowedMethods")
            if (allowedMethods && allowedMethods[actionName]) {
                httpMethod = PathItem.HttpMethod.valueOf(allowedMethods[actionName] as String)
            }
            UrlCreator urlCreator = urlMappingsHolder.getReverseMapping(controllerArtifact.logicalPropertyName, actionName,
                    controllerArtifact.pluginName, [:])
            url = urlCreator.createURL([:], "utf-8")
        }
        def pathItem = new PathItem()
        pathItem.operation(httpMethod, operation)
        openAPI.paths.addPathItem(url, pathItem)
    }

    Tag buildControllerDoc(GrailsControllerClass grailsControllerClass) {
        def tag = new Tag()
        tag.name = grailsControllerClass.logicalPropertyName.capitalize()
        if (!grailsControllerClass.actions) {
            return tag
        }
        ApiDoc apiDocAnnotation = grailsControllerClass.clazz.getAnnotation(ApiDoc) as ApiDoc
        if (!apiDocAnnotation) {
            return tag
        }
        def tagClosure = apiDocAnnotation.tag()
        if (tagClosure) {
            def tagFromClosure = processClosure(tagClosure, new TagBuilder()) as Tag
            // copy default name
            if (!tagFromClosure.name) {
                tagFromClosure.name = tag.name
            }
            tag = tagFromClosure
        }
        openAPI.addTagsItem(tag)
        tag
    }

    def processClosure(Class closureClass, AnnotationBuilder builder) {
        //def builder = builderClass.newInstance(openAPI: openAPI)
        if (closureClass) {
            // call the constructor of Closure(Object owner, Object thisObject)
            Closure closure = closureClass.newInstance(openAPI, openAPI) as Closure
            closure.delegate = builder
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure()
        }
        builder.model
    }

    void buildParameters(Operation operation, Method method, Class controllerClass) {
        if (!operation.parameters){
            operation.parameters = []
        }
        method.parameters.each {
            // automatically build primitive type schema
            if (it.type.isPrimitive() || it.type in [String, Number]) {
                operation.parameters << new Parameter(name: it.name,
                        schema: new Schema(type: it.type.simpleName.toLowerCase()))
            }
        }
    }
}
