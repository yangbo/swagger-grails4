package swagger.grails4.samples

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        "/"(controller: 'application', action:'index')
        post "/user/login"(controller: 'user', action:'login')
        post "/users"(controller: 'user', action:'createUser')
        get "/user/$id"(controller: 'user', action:'showUser')

        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
