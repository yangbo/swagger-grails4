package swagger.grails4.openapi

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * The OpenAPI annotation in Groovy Closure style.
 *
 * @author bo.yang <bo.yang@telecwin.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@GroovyASTTransformationClass(["swagger.grails4.compiler.ApiDocTransformer"])
@interface ApiDoc {
    /**
     * place-holder for any place that need a string content.
     * Used in Command object field description annotation.
     */
    String value() default ""
    /**
     * To populate OpenAPI Object fields info, tags, servers, security and externalDocs.
     * @see io.swagger.v3.oas.annotations.OpenAPIDefinition
     */
    Class openAPIDefinition() default {}
    /**
     * open api document closure of action.
     * @see io.swagger.v3.oas.annotations.Operation
     */
    Class operation() default {}
    /**
     * The annotation may be applied at class or method level, or in {@link io.swagger.v3.oas.annotations.Operation#tags()} to define tags for the
     * single operation (when applied at method level) or for all operations of a class (when applied at class level).
     * <p>It can also be used in {@link io.swagger.v3.oas.annotations.OpenAPIDefinition#tags()} to define spec level tags.</p>
     * <p>When applied at method or class level, if only a name is provided, the tag will be added to operation only;
     * if additional fields are also defined, like description or externalDocs, the Tag will also be added to openAPI.tags
     * field</p>
     *
     * reference: <a target="_new" href="https://github.com/OAI/OpenAPI-Specification/blob/3.0.1/versions/3.0.1.md#tagObject">Tag (OpenAPI specification)</a>
     *
     * @see io.swagger.v3.oas.annotations.tags.Tag
     */
    Class tag() default {}
    /**
     * document closure for security.
     * @see io.swagger.v3.oas.annotations.security.SecurityScheme
     */
    Class securityScheme() default {}
}
