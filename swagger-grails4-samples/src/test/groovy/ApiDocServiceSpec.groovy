import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import swagger.grails4.OpenApiService

class ApiDocServiceSpec extends Specification implements ServiceUnitTest<OpenApiService> {
    def "testGenerateDocument"() {
        when:
        def doc = service.generateDocument()
        println(doc)
        then:
        doc
    }
}
