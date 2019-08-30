package spud.core

interface SpudMultiSiteProvider {

	def availableSites()

	def isMultiSiteEnabled()

	def setActiveSite(siteId)

	def getActiveSite()

	def adminApplicationsForSite(siteId)

	def siteForUrl(url)
	def siteForUrl()

}
