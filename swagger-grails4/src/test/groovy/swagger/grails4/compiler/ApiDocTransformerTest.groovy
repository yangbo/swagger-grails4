package swagger.grails4.compiler

import spock.lang.Specification

class ApiDocTransformerTest extends Specification {
    def "CleanComment"() {
        when:
        String comment = ApiDocTransformer.cleanComment(rowComment)
        then:
        comment == cleaned
        where:
        rowComment | cleaned
        """
        /**
         * The Comments.
         */
        """ | "The Comments."
        """
        
        /**
         * The Comments.
         * Multiple lines.
         */
""" | "The Comments.\nMultiple lines."
    }
}
