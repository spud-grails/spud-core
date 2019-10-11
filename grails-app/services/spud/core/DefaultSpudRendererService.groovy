package spud.core

class DefaultSpudRendererService {
	static transactional = false
	def defaultSpudTemplateService

	def render(options) {
		// Grab this pages template Service and pass it on
		def spudTemplateService = options.templateService ?: defaultSpudTemplateService
		log.debug "spudTemplateService: ${spudTemplateService}"
		spudTemplateService.renderContent(options.page)
	}
}
