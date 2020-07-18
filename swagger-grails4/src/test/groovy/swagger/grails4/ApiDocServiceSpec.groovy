package swagger.grails4

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import swagger.grails4.OpenApiService

class ApiDocServiceSpec extends Specification implements ServiceUnitTest<OpenApiService> {
    def "testGenerateDocument"(){
        expect:
        service.generateDocument()
    }
}
