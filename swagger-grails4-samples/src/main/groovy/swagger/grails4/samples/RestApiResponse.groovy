package swagger.grails4.samples

import swagger.grails4.openapi.ApiDoc

/**
 * A test rest api response class
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
@ApiDoc("A test rest api response class")
class RestApiResponse {
    /**
     * Error code
     */
    int code
    /**
     * Message
     */
    String msg
    /**
     * Return payload
     */
    Object info
}
