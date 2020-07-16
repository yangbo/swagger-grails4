import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class OpenApiServiceSpec extends Specification implements ServiceUnitTest<OpenApiService> {
    def "testGenerateDocument"(){
        expect:
        service.generateDocument()
    }
}
