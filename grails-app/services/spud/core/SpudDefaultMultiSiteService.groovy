package spud.core

import org.codehaus.groovy.grails.web.util.WebUtils
import javax.servlet.http.Cookie

class SpudDefaultMultiSiteService implements SpudMultiSiteProvider {
	static transactional = false
    def grailsApplication

	def availableSites() {
		def sites = config ?: []
		sites = [[siteId: grailsApplication.config.spud.core.defaultSiteId ?: 0, name: 'Default', shortName: 'default', hosts: []]] + sites
		return sites ?: null
	}

	def isMultiSiteEnabled() {
		availableSites()?.size() > 1
	}

	def setActiveSite(siteId) {
		// Stateless Cookie
		def spudSiteCookie = new Cookie('Spud-Site-Id',siteId.toString())
		response.addCookie(spudSiteCookie)
	}

	def getActiveSite() {
		def siteIdCookie = request.cookies.find{ it.name == 'Spud-Site-Id'}
		def site
		if(!siteIdCookie) {
			site = request.getAttribute('spudSite')
		} else {
			site = siteForSiteId(siteIdCookie.value.toInteger())
		}

		if(!site) {
			return siteForUrl(request.requestURL)
		} else {
			return site
		}
		// Verify Site is available
	}

	def adminApplicationsForSite(siteId) {

	}

	def siteForUrl(url=null) {
		if(!url) {
			url = request?.requestURL.toString()
		}
		def sites = availableSites()
		if(sites) {
			def urlObject = new java.net.URL(url)
			def host = urlObject.host.toLowerCase()
			def site = sites.find{it.hosts.contains(host)}
			if(site) {
				return site
			}
		}
		return [siteId: grailsApplication.config.spud.core.defaultSiteId ?: 0, name: 'Default', shortName: 'default', hosts: []]
	}

	def siteForSiteId(siteId) {
		def sites = availableSites()
		return sites.find{it.siteId == siteId}
	}


	protected getConfig() {
		grailsApplication.config.spud.core.sites
		// Format:
		// [[siteId: 1, shortName: 'test', name: 'My Subsite', hosts: ['subsite.example.org']]]
	}

	protected getRequest() {
		try {
			def webUtils = WebUtils.retrieveGrailsWebRequest()
			return webUtils.getCurrentRequest()
		} catch(e) {
			return null
		}
	}

	protected getResponse() {
		try {
			def webUtils = WebUtils.retrieveGrailsWebRequest()
			return webUtils.getCurrentResponse()
		} catch(e) {
			return null
		}
	}
}
