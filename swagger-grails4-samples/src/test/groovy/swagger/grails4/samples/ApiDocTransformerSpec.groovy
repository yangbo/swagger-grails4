package swagger.grails4.samples

import spock.lang.Ignore
import spock.lang.Specification

/**
 * Test ApiDocTransformer
 */
class ApiDocTransformerSpec extends Specification {

    @Ignore
    def "testTrait"() {
        when:def script = '''
        import swagger.grails4.openapi.ApiDoc
        import swagger.grails4.openapi.ApiDocComment

        @ApiDoc("My Trait")
        trait MyTrait {
            /**
             * Upper Comment
             */
            /**
             * 
             * The avatar url of uer.
             * It may be empty.
             * 
             */
            @ApiDoc("My Trait")
            String avatarUrl
             // Upper of Only One Comment Block, should be ignored
            /**
             * Upper of Only One Comment Block, Should be skipped
             */
            /**
             * The name of user in comments.
             */
            String username
            /**
            * The password of user
            */
            String password
        }
        //@ApiDoc("My Command")
        class TheCommand implements MyTrait {
        }
        annotations = []
        MyTrait.declaredMethods.each { method ->
            def ann = method.getAnnotation(ApiDocComment)
            if (ann) {
                annotations << ann
            }
        }
        annotations.each {
            println it
        }
    '''
        GroovyShell shell = new GroovyShell()
        shell.evaluate(script, "script.groovy")
        def annotations = shell.context.getVariable("annotations")
        then:
        annotations[0].value() == "The avatar url of uer.It may be empty."
        annotations[1].value() == "The name of user in comments."
        annotations[2].value() == "The password of user"
    }

    def "testTransform"() {
        when:
        def script = '''
        import swagger.grails4.openapi.ApiDoc
        import swagger.grails4.openapi.ApiDocComment

        @ApiDoc("My Command")
        class MyCommand {
            /**
             * Upper Comment
             */
            /**
             * 
             * The avatar url of uer.
             * It may be empty.
             * 
             */
            String avatarUrl
             // Upper of Only One Comment Block, should be ignored
            /**
             * Upper of Only One Comment Block, Should be skipped
             */
            /**
             * The name of user in comments.
             */
            String username
            /**
            * The password of user
            */
            String password
        }
        annotations = []
        MyCommand.declaredFields.each { field ->
            def ann = field.getAnnotation(ApiDocComment)
            if (ann) {
                annotations << ann
            }
        }
        annotations.each {
            println it
        }
    '''
        GroovyShell shell = new GroovyShell()
        shell.evaluate(script, "script.groovy")
        def annotations = shell.context.getVariable("annotations")
        then:
        annotations[0].value() == "The avatar url of uer.It may be empty."
        annotations[1].value() == "The name of user in comments."
        annotations[2].value() == "The password of user"
    }

    def "test comment patterns"() {
        when:
        def slashPattern = ~$/^\s*///$
        def startStarPattern = ~$/^\s*/\*+/$
        def endStarPattern = ~$/\*/\s*/$
        // test switch with pattern object grammar
        String input = "  /* this"
        switch (input) {
            case { slashPattern.matcher(it) }:
                println "slashPattern"
                break
            case { startStarPattern.matcher(it) }:
                println "startStarPattern"
                break
            case { endStarPattern.matcher(it) }:
                println "endStarPattern"
                break
        }
        then:
        slashPattern.matcher("//this")
        slashPattern.matcher("// this")
        slashPattern.matcher("      //this")
        !slashPattern.matcher("/ /this")
        !slashPattern.matcher(" a //this")
        slashPattern.matcher("//this").replaceAll("") == "this"
        endStarPattern.matcher("*/")
        endStarPattern.matcher("*/     ")
        endStarPattern.matcher("*/\t \n")
        endStarPattern.matcher("注释\n注释*/\n     ").replaceAll("") == "注释\n注释"
        startStarPattern.matcher("/**")
        startStarPattern.matcher("/*****")
        startStarPattern.matcher("  \t  /*****")
        startStarPattern.matcher("  \t  /*****\n注释").replaceFirst("") == "\n注释"
        startStarPattern.matcher("  \t  /*注释").replaceFirst("") == "注释"
    }

    def "test comment block pattern"(){
        when:
        def snippet = """
// comment
/**
 * Upper
 */
/**
 * This is comment.
 */
"""
        def blockPattern = ~$/(?s)\s*(/\*+.*?\*/)\s*/$
        def matcher = blockPattern.matcher(snippet)
        String lastFound
        while(matcher.find()){
            lastFound = matcher.group(1)
        }
        println lastFound
        then:
        lastFound
    }
}
