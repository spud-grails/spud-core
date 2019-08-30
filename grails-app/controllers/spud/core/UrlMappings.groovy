package spud.core

class UrlMappings {
	static mappings = {
		'/spud/admin'(controller: 'dashboard', action: 'index', namespace: 'spud_admin')
		'/spud/admin/switchSite'(controller: 'dashboard', action: 'switchSite', namespace: 'spud_admin')
	}
}
