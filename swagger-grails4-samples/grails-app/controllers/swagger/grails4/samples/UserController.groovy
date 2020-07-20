package swagger.grails4.samples


import grails.rest.*
import grails.converters.*
import swagger.grails4.openapi.ApiDoc

@ApiDoc({
    description "用户相关API"
})
class UserController {
	static responseFormats = ['json', 'xml']

    @ApiDoc(operation = {
        summary "列出用户"
        description "列出用户，可以带查询条件、翻页参数"
    })
    def index() { }
}
