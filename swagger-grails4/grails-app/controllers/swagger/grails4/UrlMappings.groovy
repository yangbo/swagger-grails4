package swagger.grails4

/**
 * Url mapping config of grails
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
