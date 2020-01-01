package spud.core

class SpudLayoutService {
	static transactional = false
	def grailsApplication

	def layoutServiceForSite(siteId = 0) {
		return layoutServiceByName('system')
	}

	def activeLayoutService(name = 'system') {
		if(name) {
			return layoutServiceByName(name)
		}

		return layoutServiceByName('system')
	}

	private layoutServiceByName(key) {
		log.debug "layoutServiceByName key: ${key}"
		def engineName = grailsApplication.config.spud.layoutEngines[key]
		if(engineName) {
			return grailsApplication.mainContext[engineName]
		} else {
			return grailsApplication.mainContext['defaultSpudLayoutService']
//			return null
		}
	}
}
