package swagger.grails4.openapi


import grails.core.GrailsApplication
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiReader
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.tags.Tag
import swagger.grails4.openapi.builder.OperationBuilder

import java.lang.reflect.Method

/**
 * Groovy annotation reader for OpenAPI
 */
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
     * 读指定的类，生成 OpenAPI
     *
     * @param classes 要读的类
     * @param resources TODO 搞清楚是什么含义
     * @return
     */
    @Override
    OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        classes.each {
            processApiDocAnnotation(it)
        }
        openAPI
    }

    def processApiDocAnnotation(Class controllerClass) {
        controllerClass.metaClass.metaMethods.each {
            Method method = it.cachedMethod
            def apiDoc = method.getAnnotation(ApiDoc)
            // operation
            // call the constructor of Closure(Object owner, Object thisObject)
            Closure closure = apiDoc.operation() as Closure
            def operationBuilder = new OperationBuilder()
            def operation = closure.rehydrate(operationBuilder, openAPI, openAPI)
            closure.resolveStrategy = DELEGATE_FIRST
            operation()
        }
    }
}
