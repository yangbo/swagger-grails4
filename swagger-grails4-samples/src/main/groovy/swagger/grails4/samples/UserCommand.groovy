package swagger.grails4.samples

import grails.validation.Validateable
import swagger.grails4.openapi.ApiDoc

@ApiDoc("The command contains User properties")
class UserCommand implements Validateable{
    /**
     * The name of user in comments.
     */
    @ApiDoc("The name of user")
    String username
    @ApiDoc("The password of user")
    String password
    @ApiDoc("The avatar url of user")
    String avatarUrl
    /**
     * Area of user
     */
    MyEnum area

    // Upper of Only One Comment Block, should be ignored
    /**
     * Upper of Only One Comment Block, Should be skipped
     */
    /**
     * Only One Comment Block Should be extracted.
     */
    String onlyOneCommentBlock
}
