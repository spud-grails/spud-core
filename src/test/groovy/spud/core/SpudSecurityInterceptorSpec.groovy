package spud.core


import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.testing.web.interceptor.InterceptorUnitTest} for usage instructions
 */
class SpudSecurityInterceptorSpec extends Specification implements InterceptorUnitTest<SpudSecurityInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test spudSecurity interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(uri: '/spud/admin/**')

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
