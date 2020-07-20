package swagger.grails4

/**
 * Url mapping config of grails
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
class UrlMappings {

    static mappings = {
        "/api"(controller: "openApi", action: "index")
        "/api/doc"(controller: "openApi", action: "document")
    }
}
