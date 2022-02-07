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
        responses "200": {
            content "default": {
                description "success response"
                schema RestApiResponse
            }
        }, "201": {
            content "default": {
                description "success response with 201"
                schema UserCommand
            }
        }
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
        responses "200": {
            content "default": {
                description "success response"
                schema RestApiResponse, properties: [info: UserCommand]
            }
        }
    })
    def login(String password, String username) {
        log.debug("$username, $password")
        [username: username, password: password]
    }

    @ApiDoc(operation = {
        summary "Create User"
        description "Create a new user"
        responses "200": {
            content "default": {
                description "success response"
                schema {
                    name "CustomSchema"
                    type "string"
                    description "The customized json response"
                }
            }
        }
    })
    def createUser(UserCommand command) {
    }

    @ApiDoc(operation = {
        summary "Show User"
        description "Create a new user"
        parameters([{
                        name "id"
                        description "User id"
                        inType "path"
                        schema { type "string" }
                    }])
        responses "200": {
            content "default": {
                description "success response"
                schema {
                    name "CustomSchema"
                    type "string"
                    description "The customized json response"
                }
            }
        }
    })
    def showUser(String id) {
    }
}
