package spud.core.admin
import  spud.core.*


@SpudSecure(['AUTHORIZED'])
class DashboardController {
	static namespace = 'spud_admin'

    def index() {
    	def adminApplications = grailsApplication.config.spud.core.adminApplications
    	
    	render view: '/spud/admin/dashboard/index', model: [adminApplications: adminApplications, breadCrumbs:[["Dashboard", "/spud/admin"]]]
    }
}
