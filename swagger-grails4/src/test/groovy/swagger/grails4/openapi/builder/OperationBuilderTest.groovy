package swagger.grails4.openapi.builder

import spock.lang.Specification

class OperationBuilderTest extends Specification {

    def "test annotationToProperties"(){
        when:
        String summary = "This is summary"
        OperationBuilder operationBuilder = new OperationBuilder()
        operationBuilder.summary summary
        then:
        operationBuilder.model.summary == summary
        operationBuilder.model.operationId == ""
        !operationBuilder.model.deprecated
    }
}
