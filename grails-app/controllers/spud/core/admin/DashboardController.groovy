package spud.core.admin
import  spud.core.*


@SpudSecure(['AUTHORIZED'])
class DashboardController {
	static namespace = 'spud_admin'
	def adminApplicationService

	def spudMultiSiteService

    def index() {
    	def adminApplications = adminApplicationService.myApplications()

    	render view: '/spud/admin/dashboard/index', model: [adminApplications: adminApplications, breadCrumbs:[["Dashboard", "/spud/admin"]]]
    }


	def switchSite() {
		println "Params ${params}"
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
