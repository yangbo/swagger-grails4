package swagger.grails4.samples

import grails.validation.Validateable

class UserCommand implements Validateable{
    String username
    String password
    String avatarUrl
}
