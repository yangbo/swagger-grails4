package swagger.grails4.openapi

import grails.artefact.DomainClass
import grails.core.GrailsApplication
import grails.core.GrailsControllerClass
import grails.gorm.validation.ConstrainedProperty
import grails.validation.Validateable
import grails.web.Action
import grails.web.mapping.UrlCreator
import grails.web.mapping.UrlMapping
import grails.web.mapping.UrlMappingsHolder
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiReader
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.tags.Tag
import org.grails.web.mapping.RegexUrlMapping
import swagger.grails4.openapi.builder.AnnotationBuilder
import swagger.grails4.openapi.builder.OperationBuilder
import swagger.grails4.openapi.builder.TagBuilder

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.regex.Matcher

/**
 * Groovy annotation reader for OpenAPI
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
@Slf4j
class Reader implements OpenApiReader {

    final static String DEFAULT_MIME = "*/*"
    final static String JSON_MIME = "application/json"

    OpenAPIConfiguration config
    GrailsApplication application

    private OpenAPI openAPI = new OpenAPI()

    @CompileStatic
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
    @CompileStatic
    OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        classes.each {
            processApiDocAnnotation(it)
        }
        // sort controller by tag name
        openAPI.tags = openAPI.tags?.sort { it.name }
        openAPI
    }

    @CompileStatic
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
            def operationBuilder = new OperationBuilder(reader: this)
            // resolve grails action command parameters
            operationBuilder.model.requestBody = buildActionCommandParameters(actionName, controllerArtifact, urlMappingsHolder)
            // process operation closure that can override parameters information
            def operation = processClosure(closureClass, operationBuilder) as Operation
            operation.addTagsItem(controllerTag.name)
            buildPathItem(operation, actionName, controllerArtifact, urlMappingsHolder)
        }
    }

    @CompileStatic
    def buildPathItem(Operation operation, String actionName, GrailsControllerClass controllerArtifact, UrlMappingsHolder urlMappingsHolder) {
        // Resolve http method, url from:
        // 1. UrlMapping rule
        // 2. Controller allowedMethods map
        // 3. default as GET

        // 1. from UrlMapping
        UrlMapping urlMappingOfAction = getUrlMappingOfAction(urlMappingsHolder, controllerArtifact, actionName)
        PathItem.HttpMethod httpMethod = PathItem.HttpMethod.GET
        String url
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
            //Try to replace asterisk placeholders of path parameters
            if (urlMappingOfAction instanceof RegexUrlMapping) {
                urlMappingOfAction.constraints.each { def constrainedProperty ->
                    //Replace optional placeholder first
                    url = url.replaceFirst("\\(\\(\\*\\)\\)\\?", "\\(\\*\\)")
                    //Then replace variables
                    url = url.replaceFirst("\\(\\*\\)", '{' + ((ConstrainedProperty) constrainedProperty).propertyName + '}')
                }
            }
        } else {
            // 2. from controller
            def allowedMethods = controllerArtifact.getPropertyValue("allowedMethods")
            if (allowedMethods && allowedMethods[actionName]) {
                httpMethod = PathItem.HttpMethod.valueOf(allowedMethods[actionName] as String)
            }
            def controllerName = controllerArtifact.logicalPropertyName
            UrlCreator urlCreator = urlMappingsHolder.getReverseMapping(controllerName, actionName,
                    controllerArtifact.pluginName, [:])
            url = urlCreator.createURL([controller: controllerName, action: actionName], "utf-8")
        }
        def pathItem = new PathItem()
        pathItem.operation(httpMethod, operation)
        openAPI.paths.addPathItem(url, pathItem)
    }

    @CompileStatic
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
            def tagFromClosure = processClosure(tagClosure, new TagBuilder(reader: this)) as Tag
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

    /**
     * Grails controller original action has an annotation "Action" with member "commandObjects" store
     * action commands classes, we will build parameter schemas on these command class.
     * @param actionName name of the action to build
     * @param grailsControllerClass action belonged grails controller class
     */
    @CompileStatic
    RequestBody buildActionCommandParameters(String actionName, GrailsControllerClass grailsControllerClass, UrlMappingsHolder urlMappingsHolder) {
        Class plainClass = grailsControllerClass.clazz
        def actionMethods = plainClass.methods.find { it.name == actionName && it.getAnnotation(Action) }
        def actionAnnotation = actionMethods.getAnnotation(Action)
        def commandClasses = actionAnnotation.commandObjects()
        if (commandClasses) {
            // Create schema in components
            def commandClass = commandClasses[0]
            if (!isCommandClass(commandClass)) {
                return null
            }

            // If it is a GET request, do not add command class as request body
            UrlMapping urlActionMapping = getUrlMappingOfAction(urlMappingsHolder, grailsControllerClass, actionName)
            if (urlActionMapping && urlActionMapping.httpMethod == 'GET') {
                return null
            }

            Schema schema = buildSchema(commandClass)
            def ref = getRef(schema)
            Content content = new Content()
            content.addMediaType(JSON_MIME, new MediaType(schema: new Schema($ref: ref)))
            content.addMediaType(DEFAULT_MIME, new MediaType(schema: new Schema($ref: ref)))
            return new RequestBody(content: content)
        } else {
            return null
        }
    }

    @CompileStatic
    static String getRef(Schema schema) {
        "#/components/schemas/${schema.name}"
    }

    @CompileStatic
    Map<String, Schema> buildClassProperties(Class<?> aClass) {
        SortedMap<String, Schema> propertiesMap = new TreeMap<>()
        aClass.metaClass.properties.each { MetaProperty metaProperty ->
            if (!(metaProperty.modifiers & Modifier.PUBLIC)) {
                return
            }
            String fieldName = metaProperty.name
            Class fieldType = metaProperty.type
            Field field = null
            if (metaProperty instanceof MetaBeanProperty) {
                field = metaProperty.field?.field
            }
            // skip grails/groovy fields
            switch (fieldName) {
                case ~/.*(grails_|\$).*/:
                case "metaClass":
                case "properties":
                case "class":
                case "clazz":
                case "constraints":
                case "constraintsMap":
                case "mapping":
                case "log":
                case "logger":
                case "instanceControllersDomainBindingApi":
                case "instanceConvertersApi":
                case "errors":
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "version" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "transients" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "all" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "attached" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "belongsTo" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "constrainedProperties" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "dirty" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "dirtyPropertyNames" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "gormDynamicFinders" }:
                case { DomainClass.isAssignableFrom(aClass) && fieldName == "gormPersistentEntity" }:
                    return
            }
            Schema schema = getSchemaFromOpenAPI(fieldType)
            if (!schema) {
                schema = buildSchema(fieldType, field?.genericType)
                // TODO get annotations from field or trait getter method
                // @ApiDoc prefer over @ApiDocComment
                def apiDocAnn = field?.getAnnotation(ApiDoc)
                def apiDocCommentAnn = field?.getAnnotation(ApiDocComment)
                def comments = apiDocAnn ? apiDocAnn.value() : apiDocCommentAnn?.value()
                comments = comments ?: ""
                if (schema.description) {
                    schema.description = comments + " \n" + schema.description
                } else {
                    schema.description = comments
                }
            }
            propertiesMap[fieldName] = schema
        }
        return propertiesMap
    }

    /**
     * Build Schema from command class or domain class
     * @param aClass command class, domain class
     * @param genericType the genericType of the aClass, such as a Collection<Type> class
     * @return OAS Schema object
     */
    Schema buildSchema(Class aClass, Type genericType = null) {
        TypeAndFormat typeAndFormat = buildType(aClass)
        // check exists schema, avoid infinite loop
        Schema schema = getSchemaFromOpenAPI(aClass)
        if (schema) {
            return schema
        }
        String name = schemaNameFromClass(aClass)
        Map args = [name       : name,
                    type       : typeAndFormat.type,
                    format     : typeAndFormat.format,
                    description: buildSchemaDescription(aClass)]
        schema = typeAndFormat.type == "array" ? new ArraySchema(args) : new Schema(args)
        if (typeAndFormat.type in ["object", "enum"]) {
            openAPI.schema(aClass.canonicalName, schema)
        }
        switch (typeAndFormat.type) {
            case "object":
                // skip java.xxx/org.grails.xxx/org.springframework.xxx package class
                String packageName = aClass.package?.name
                if (packageName?.startsWith('java.') ||
                        packageName?.startsWith('org.grails.') ||
                        packageName?.startsWith('org.springframework.')
                ) {
                    return schema
                }
                schema.properties = buildClassProperties(aClass)
                // cut referencing cycle
                schema.properties.each {
                    cutReferencingCycle(it.value)
                }
                break
            case "array":
                // try to get array element type
                Class itemClass = aClass.componentType
                // extract item type for collections
                if (!itemClass && Collection.isAssignableFrom(aClass) && genericType instanceof ParameterizedType) {
                    itemClass = genericType.actualTypeArguments[0] as Class
                } else {
                    itemClass = itemClass ?: Object
                }
                if (itemClass && schema instanceof ArraySchema) {
                    schema.items = buildSchema(itemClass)
                    // for swagger-ui hang-up bug
                    cutReferencingCycle(schema.items)
                }
                break
            case "enum":
                schema.type = "integer"
                schema.setEnum(buildEnumItems(aClass))
                buildEnumDescription(aClass, schema)
                break
        }
        return schema
    }

    /**
     * Build OASv3 type and format from class.
     */
    @CompileStatic
    static TypeAndFormat buildType(Class aClass) {
        TypeAndFormat typeAndFormat = new TypeAndFormat()
        switch (aClass) {
            case String:
            case GString:
                typeAndFormat.type = "string"
                break
            case short:
            case Short:
                typeAndFormat.type = "integer"
                break
            case int:
            case Integer:
                typeAndFormat.type = "integer"
                typeAndFormat.format = "int32"
                break
            case long:
            case Long:
                typeAndFormat.type = "integer"
                typeAndFormat.format = "int64"
                break
            case boolean:
            case Boolean:
                typeAndFormat.type = "boolean"
                break
            case double:
            case Double:
                typeAndFormat.type = "number"
                typeAndFormat.format = "double"
                break
            case float:
            case Float:
                typeAndFormat.type = "number"
                typeAndFormat.format = "float"
                break
            case Number:
                typeAndFormat.type = "number"
                break
            case Collection:
            case { aClass.isArray() }:
                typeAndFormat.type = "array"
                break
            case Enum:
                typeAndFormat.type = "enum"
                break
            case Date:
                typeAndFormat.type = "string"
                typeAndFormat.format = "date-time"
                break
            default:
                typeAndFormat.type = "object"
                break
        }
        return typeAndFormat
    }

    @CompileStatic
    static boolean isCommandClass(Class<?> aClass) {
        Validateable.isAssignableFrom aClass
    }

    @CompileStatic
    static String buildSchemaDescription(Class aClass) {
        ApiDoc apiDocAnnotation = aClass.getAnnotation(ApiDoc) as ApiDoc
        apiDocAnnotation?.value() ?: ""
    }

    /**
     * Get a clone schema from openApi
     * @param aClass class to find in openApi
     * @param clone true means clone the schema object if it is found in openApi
     * @return the found schema object or null
     */
    @CompileStatic
    Schema getSchemaFromOpenAPI(Class aClass, boolean clone = true) {
        def name = schemaNameFromClass(aClass)
        def schema = openAPI.components?.getSchemas()?.get(name)
        if (schema && clone) {
            schema = cloneSchema(schema)
            schema.$ref = getRef(schema)
            // remove properties to prevent cycle referencing
            schema.properties = [:]
        }
        schema
    }

    /**
     * Use enum id as property value
     */
    static List buildEnumItems(Class enumClass) {
        enumClass.values()?.collect {
            // if has id property then use it, otherwise use enum name
            if (it.hasProperty("id")) {
                it.id
            } else {
                it.name()
            }
        }
    }

    static void buildEnumDescription(Class aClass, Schema schema) {
        StringBuilder builder = new StringBuilder(schema.description)
        if (schema?.description?.trim()) {
            char endChar = schema.description.charAt(schema.description.length() - 1)
            if (Character.isAlphabetic(endChar) || Character.isIdeographic(endChar)) {
                builder.append(". ")
            }
        }
        builder.append("Enum of: ")
        aClass.values()?.eachWithIndex { enumValue, idx ->
            String idPart = ""
            if (enumValue.hasProperty("id")) {
                idPart = "(${enumValue.id})"
            }
            // append ", " if idx > 0
            if (idx > 0) {
                builder.append(", ")
            }
            builder.append("${enumValue.name()}${idPart}")
        }
        schema.description = builder.toString()
    }

    @CompileStatic
    static Schema cloneSchema(Schema schema) {
        Schema clone = new Schema()
        schema.metaClass.properties.each { prop ->
            // only assign writable property
            def setMethod = Schema.methods.find {
                it.name == "set${prop.name.capitalize()}"
            }
            if (setMethod) {
                clone[prop.name] = schema[prop.name]
            }
        }
        clone
    }

    @CompileStatic
    static String schemaNameFromClass(Class aClass) {
        aClass.canonicalName
    }

    /**
     * check if schema is in properties referencing cycle
     * @param schema Schema or ArraySchema
     * @return true the schema is in the reference cycle
     */
    boolean isCycleReferencing(Schema schema, String targetName = null) {
        if (schema instanceof ArraySchema) {
            schema = schema.items
        }
        if (targetName && targetName == schema.name) {
            return true
        }
        // iterate schema properties and check if targetName can be reached by schema referencing
        boolean found = false
        // by $ref first
        if (schema.$ref) {
            schema = getSchemaBy$Ref(openAPI, schema.$ref)
        }
        schema?.properties?.each {
            def propSchema = it.value
            targetName = targetName ?: schema.name
            if (!found && isCycleReferencing(propSchema, targetName)) {
                found = true
            }
        }
        return found
    }

    static Schema getSchemaBy$Ref(OpenAPI openAPI, String ref) {
        Matcher m = (ref =~ $/#/components/schemas/(.+)/$)
        if (!m) {
            return null
        }
        def schemaName = m.group(1)
        openAPI.components.schemas.find {
            it.key == schemaName
        }?.value
    }

    void cutReferencingCycle(Schema schema) {
        // because swagger-ui hang-up when show cycle referencing schemas,so we will cut these referencing
        if (isCycleReferencing(schema)) {
            if (schema instanceof ArraySchema) {
                schema = schema.items
            }
            def props = new StringBuilder("should have properties: ")
            schema.properties.eachWithIndex { it, idx ->
                props.append(idx > 0 ? ", " : "")
                props.append("${it.key}(${it.value.name})")
            }
            schema.description = schema?.description + "${schema.name} [no properties/\$ref for swagger-ui bug, ${props}]"
            schema.properties = [:]
            schema.$ref = null
        }
    }

    /**
     * According to the https://swagger.io/docs/specification/data-models/data-types/
     */
    @CompileStatic
    static class TypeAndFormat {
        String type = "object"
        String format = null
    }

    private UrlMapping getUrlMappingOfAction(UrlMappingsHolder urlMappingsHolder, controllerArtifact, String actionName) {
        def urlMappingOfAction = urlMappingsHolder.urlMappings.find {
            it.controllerName == controllerArtifact.logicalPropertyName && it.actionName == actionName
        }
        return urlMappingOfAction
    }

}
