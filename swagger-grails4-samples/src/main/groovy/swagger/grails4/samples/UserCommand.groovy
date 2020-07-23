package swagger.grails4.samples

import grails.validation.Validateable
import swagger.grails4.openapi.ApiDoc

@ApiDoc("The command contains User properties")
class UserCommand implements Validateable{
    @ApiDoc("The name of user")
    String username
    @ApiDoc("The password of user")
    String password
    @ApiDoc("The avatar url of user")
    String avatarUrl
}