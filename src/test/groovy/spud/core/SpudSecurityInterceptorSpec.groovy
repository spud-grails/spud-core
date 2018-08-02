package spud.core


import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(SpudSecurityInterceptor)
class SpudSecurityInterceptorSpec extends Specification {

    def setup() {
    }

    def cleanup() {

    }

    void "Test spudSecurity interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"spudSecurity")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
