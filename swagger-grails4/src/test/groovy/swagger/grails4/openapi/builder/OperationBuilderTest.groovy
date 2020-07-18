package swagger.grails4.openapi.builder

import spock.lang.Specification

class OperationBuilderTest extends Specification {

    def "test annotationToProperties"(){
        when:
        OperationBuilder operationBuilder = new OperationBuilder()
        then:
        operationBuilder.model.summary == ""
        operationBuilder.model.operationId == ""
        !operationBuilder.model.deprecated
    }
}
