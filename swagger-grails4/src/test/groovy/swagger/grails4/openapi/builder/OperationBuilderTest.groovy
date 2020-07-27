package swagger.grails4.openapi.builder

import spock.lang.Specification
import swagger.grails4.RestApiResponse
import swagger.grails4.openapi.Reader

class OperationBuilderTest extends Specification {

    def "test annotationToProperties"() {
        when:
        String summary = "This is summary"
        OperationBuilder operationBuilder = new OperationBuilder()
        operationBuilder.summary summary
        then:
        operationBuilder.model.summary == summary
        operationBuilder.model.operationId == ""
        !operationBuilder.model.deprecated
    }

    def "test responses member"() {
        when:
        Reader reader = new Reader()
        OperationBuilder operationBuilder = new OperationBuilder(reader: reader)
        operationBuilder.evaluateClosure({
            responses "200": {
                content "default": {
                    description "success response"
                    schema RestApiResponse
                }
            }
        }, operationBuilder)

        then:
        println operationBuilder.model
        operationBuilder.model.responses
        operationBuilder.model.responses["200"].content["default"].schema.name == RestApiResponse.name
    }
}
