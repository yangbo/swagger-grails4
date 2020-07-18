package swagger.grails4

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import swagger.grails4.openapi.ApiDoc
import swagger.grails4.openapi.OpenApiConfig

/**
 * Sample controller with @OpenApi annotations
 */

// 全局配置，只需要出现一次
@ApiDoc(openAPIDefinition = {
//    info {
//        description "This is a sample api"
//        version "1.0.5"
//        title "Telecwin API Sample"
//        termsOfService "http://your.domain.com/terms/"
//        contact {
//            email "apiteam@telecwin.com"
//        }
//        license {
//            name "Apache 2.0"
//            url "http://www.apache.org/licenses/LICENSE-2.0.html"
//        }
//    }
//    host "api.telecwin.com"
//    basePath "v1"
//    security([
//            token : [type        : 'http',
//                     scheme      : "bearer",
//                     bearerFormat: "JWT"],
//            apikey: [
//                    type: "apiKey",
//                    name: "api_key",
//                    in  : "header"]
//    ])
})
class SampleController {
    @ApiDoc(operation = {
        summary "List sample data"
        responses([{
                       responseCode 200
                       description "success"
                       content {
                           schema RestApiResponse
                       }
                   }])
    })
    def index() {
    }
}
