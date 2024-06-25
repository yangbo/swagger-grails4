package swagger.grails4.samples

import grails.converters.JSON
import swagger.grails4.openapi.ApiDoc

@ApiDoc(tag = {
    description "Test API"
})
class TesteController {
    static responseFormats = ['json', 'xml']

    @ApiDoc(operation = {
        summary "Test"
        description "Teste de swagger"
        parameters([{
                        name "s"
                        description "Texto qualquer"
                        inType "path"
                        schema { "string" }
                    }])
        responses "200": {
            content "application/json": {
                description "success response"
                schema {
                    name "Resposta"
                    type "string"
                    description "Texto digitado"
                }
            }
        }
    })
    def teste(String s) {
        render([text: s]as JSON)
    }
}