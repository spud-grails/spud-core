package spud.core.admin

import spud.core.*

@SpudSecure(['AUTHORIZED'])
class DashboardController {
	static namespace = 'spud_admin'
	def adminApplicationService

	def spudMultiSiteService

    def index() {
		log.debug "index params: ${params}"
		log.debug "index grailsApplication.config.spud.core.adminApplications: ${grailsApplication.config.spud.core.adminApplications}"
    	def adminApplications = adminApplicationService.myApplications()
		log.debug "index adminApplications: ${adminApplications}"
    	render view: '/spud/admin/dashboard/index', model: [adminApplications: adminApplications, breadCrumbs:[["Dashboard", "/spud/admin"]]]
    }

	def switchSite() {
		log.debug "switchSite params: ${params}"
		def siteId = params.long('multiSiteSelect')
		def site = spudMultiSiteService.availableSites().find{it.siteId.toLong() == siteId}
		if(site) {
			spudMultiSiteService.activeSite = site.siteId
		} else {
			flash.error = "Site Not Found"
		}
		redirect(uri: request.getHeader('referer') )
	}
}
