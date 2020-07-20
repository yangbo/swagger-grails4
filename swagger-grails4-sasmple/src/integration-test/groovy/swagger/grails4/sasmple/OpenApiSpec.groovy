package swagger.grails4.sasmple


import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification
import swagger.grails4.OpenApiService

@Integration
@Rollback
class OpenApiSpec extends Specification {

    OpenApiService openApiService

    def setup() {
    }

    def cleanup() {
    }

    void "test openApiService"() {
        when:
        def openApi = openApiService.generateDocument()
        println(openApi)
        then:
        openApi
    }
}
