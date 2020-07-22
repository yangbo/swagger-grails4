package swagger.grails4.samples

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
    def index() {}

    @ApiDoc(operation = {
        summary "Login"
        description "Login with user name and password"
        parameters([{
                        name "username"
                        description "User Name"
                        inType "query"
                        schema { type "string" }
                    }, {
                        name "password"
                        description "Password"
                        inType "query"
                        schema { type "string" }
                    }])
    })
    def login(String password, String username) {
        log.debug("$username, $password")
        [username: username, password: password]
    }

    @ApiDoc(operation = {
        summary "Create User"
        description "Create a new user"
    })
    def createUser(UserCommand command) {
    }
}
