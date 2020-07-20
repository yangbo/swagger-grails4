package swagger.grails4.samples


import grails.rest.*
import grails.converters.*
import swagger.grails4.openapi.ApiDoc

@ApiDoc(tag = {
    description "User API"
})
class UserController {
	static responseFormats = ['json', 'xml']

    @ApiDoc(operation = {
        summary "List Users"
        description "List users, support query and paging parameters"
    })
    def index() { }

    @ApiDoc(operation = {
        summary "Login"
        description "Login with user name and password"
    })
    def login(String username, String password) {
    }
}
