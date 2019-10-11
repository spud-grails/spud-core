package spud.core


class SpudSecurityInterceptor {

	def sharedSecurityService

	SpudSecurityInterceptor() {
		match(uri: '/spud/admin/**')
	}

	/**
	 * Filters all admin url mappings and verifies restrictions in controller defined by the @SpudSecure annotation
	 */
    boolean before() {
		def controllerClass = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)

		def action
		if(controllerClass) {
			action = controllerClass.clazz.declaredMethods.find { it.name == actionName }
			if(!action) {
				action = applicationContext.getBean(controllerClass.fullName).class.declaredFields.find { field -> field.name == actionName }
			}
		}
		def annotation = action?.getAnnotation(SpudSecure)

		if(!annotation && controllerClass) {
			annotation = controllerClass.clazz.getAnnotation(SpudSecure)
		}

		if(!annotation) {
			return true  //No Security Restrictions
		}

		if(!sharedSecurityService.hasAnyRole(annotation.value().collect { "SPUD_${it} "})) {
			log.debug "annotation: ${annotation}"
			log.debug "request: ${request}"
			sharedSecurityService.storeLocation(request)
			redirect(sharedSecurityService.createLink('login'))
			return false
		}
		return true
	}

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
