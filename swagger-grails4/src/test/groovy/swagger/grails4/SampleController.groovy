package swagger.grails4

import groovy.transform.CompileStatic
import swagger.grails4.openapi.OpenApi
import swagger.grails4.openapi.OpenApiConfig

/**
 * Sample controller with @OpenApi annotations
 */

// 全局配置，只需要出现一次
@OpenApiConfig({
    info {
        description "This is a sample api"
        version "1.0.5"
        title "Telecwin API Sample"
        termsOfService "http://your.domain.com/terms/"
        contact {
            email "apiteam@telecwin.com"
        }
        license {
            name "Apache 2.0"
            url "http://www.apache.org/licenses/LICENSE-2.0.html"
        }
    }
    host "api.telecwin.com"
    basePath "v1"
    security([
            token : [type        : 'http',
                     scheme      : "bearer",
                     bearerFormat: "JWT"],
            apikey: [
                    type: "apiKey",
                    name: "api_key",
                    in  : "header"]
    ])
})

@OpenApi(controller = true,
        // 这里放一些 actions 共用的信息
        value = {
        }
)

@CompileStatic
class SampleController {

    @OpenApi({
        path '/'
    })
    def index() {
    }
}
