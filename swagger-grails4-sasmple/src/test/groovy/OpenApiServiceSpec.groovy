import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import swagger.grails4.OpenApiService

class OpenApiServiceSpec extends Specification implements ServiceUnitTest<OpenApiService> {
    def "testGenerateDocument"(){
        expect:
        service.generateDocument()
    }
}
