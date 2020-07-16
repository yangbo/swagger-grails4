package swagger.grails4.openapi

import io.swagger.v3.oas.integration.api.OpenAPIConfiguration
import io.swagger.v3.oas.integration.api.OpenApiReader
import io.swagger.v3.oas.models.OpenAPI

/**
 * Groovy annotation reader for OpenAPI
 */
class Reader implements OpenApiReader {
    OpenAPIConfiguration openApiConfiguration

    @Override
    void setConfiguration(OpenAPIConfiguration openApiConfiguration) {
        this.openApiConfiguration = openApiConfiguration
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
        return null
    }
}
