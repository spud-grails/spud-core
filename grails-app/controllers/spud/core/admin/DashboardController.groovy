package spud.core.admin
import  spud.core.*


@SpudSecure(['AUTHORIZED'])
class DashboardController {
	static namespace = 'spud_admin'
	def adminApplicationService

    def index() {
    	def adminApplications = adminApplicationService.myApplications()
    	
    	render view: '/spud/admin/dashboard/index', model: [adminApplications: adminApplications, breadCrumbs:[["Dashboard", "/spud/admin"]]]
    }
}
