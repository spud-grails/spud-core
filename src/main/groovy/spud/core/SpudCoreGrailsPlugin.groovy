package spud.core

import grails.plugins.*
import groovy.util.logging.Slf4j
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.Ordered
import org.springframework.util.ClassUtils
import org.springframework.web.servlet.i18n.CookieLocaleResolver
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor

@Slf4j
class SpudCoreGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.7 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Spud Core Plugin" // Headline display name of the plugin
    def author = "David Estes"
    def authorEmail = "destes@bcap.com"
    def description = '''\
Spud Admin is a dependency package that adds a nice looking administrative panel to any project you add it to. It supports easy grails app integration and provides core functionality for spud modules.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "https://github.com/spud-grails/spud-core"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]
	def organization    = [name: "Bertram Labs", url: "http://www.bertramlabs.com/"]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]
	def issueManagement = [system: "GITHUB", url: "https://github.com/spud-grails/spud-core/issues"]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]
	def scm             = [url: "https://github.com/spud-grails/spud-core"]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)

		def application = grailsApplication
		def config = application.config
/*
		messageSource(ReloadableResourceBundleMessageSource) {
			basename = "WEB-INF/grails-app/i18n/messages"
		}
		localeChangeInterceptor(LocaleChangeInterceptor) {
			paramName = "lang"
		}
		localeResolver(CookieLocaleResolver)
*/
		def beanName = grailsApplication.config.spud.securityService ? grailsApplication.config.spud.securityService : 'abstractSpudSecurityService'

		springConfig.addAlias "spudSecurity", beanName

		def multiSiteServiceName = grailsApplication.config.spud.multiSiteService ?: 'spudDefaultMultiSiteService'
		springConfig.addAlias "spudMultiSiteService", multiSiteServiceName

//		grailsApplication.config.spud.renderers = grailsApplication.config.spud.renderers ?: [:]
//		grailsApplication.config.spud.layoutEngines = grailsApplication.config.spud.layoutEngines ?: [:]
//		grailsApplication.config.spud.renderers.gsp = 'defaultSpudRendererService'
//		grailsApplication.config.spud.layoutEngines.system = 'defaultSpudLayoutService'
/*		grailsApplication.config.spud.formatters = [
			[name: 'html', description: 'Formatted HTML'],
			[name: 'raw', description: 'RAW HTML']
		]
*/
//		webCacheKeyGenerator(SpudCacheWebKeyGenerator)

		// Load In Cached Layout List
		if(grailsApplication.warDeployed) {
			grailsApplication.config.spud.core.layouts = []
			def layoutList = grailsApplication.parentContext.getResource("WEB-INF/spudLayouts.txt")
			if(layoutList.exists()) {

				def contents = layoutList.inputStream.text
				if(contents) {
					grailsApplication.config.spud.core.layouts = contents.split("\n")
				}
			}
		}

		def catchAllMapping = ["/*".toString()]

		ClassLoader classLoader = application.classLoader
		Class registrationBean = ClassUtils.isPresent("org.springframework.boot.web.servlet.FilterRegistrationBean", classLoader ) ?
			ClassUtils.forName("org.springframework.boot.web.servlet.FilterRegistrationBean", classLoader) :
			ClassUtils.forName("org.springframework.boot.context.embedded.FilterRegistrationBean", classLoader)

		spudMultiSiteFilter(registrationBean) {
			filter = new SpudMultiSiteFilter()
			urlPatterns = catchAllMapping
			order = Ordered.LOWEST_PRECEDENCE - 9999
		}
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
		String.metaClass.camelize = {
			delegate.split("-").inject(""){ before, word ->
				before += word[0].toUpperCase() + word[1..-1]
			}
		}

		String.metaClass.underscore = {
			def output = delegate.replaceAll("-","_")
			output.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()
		}

		String.metaClass.humanize = {
			def output = delegate.replaceAll(/[\_\-]+/," ")
		}

		String.metaClass.parameterize = {
			def output = delegate.replaceAll(/[^A-Za-z0-9\-_]+/,"-").toLowerCase()
		}

		String.metaClass.titlecase = {
			def output = delegate.replaceAll( /\b[a-z]/, { it.toUpperCase() })
		}
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
		applicationContext.adminApplicationService.initialize()
    }

	def getWebXmlFilterOrder() {
		["SpudMultiSiteFilter": FilterManager.URL_MAPPING_POSITION + 999]
	}

	def doWithWebDescriptor = { xml ->
		def filters = xml.filter[0]
		filters + {
			'filter' {
				'filter-name'('SpudMultiSiteFilter')
				'filter-class'('spud.core.SpudMultiSiteFilter')
			}
		}

		def mappings = xml.'filter-mapping'[0]
		mappings + {
			'filter-mapping' {
				'filter-name'('SpudMultiSiteFilter')
				'url-pattern'("/*")
				dispatcher('REQUEST')
			}
		}
	}

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
		if(log.isDebugEnabled()) {
			if(event.application) {
				log.debug "onChange event: ${event?.application?.dump()}"
			} else {
				log.debug "onChange event: ${event?.dump()}"
			}
		}
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
		if(log.isDebugEnabled()) {
			log.debug "onConfigChange event: ${event?.dump()}"
		}
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
