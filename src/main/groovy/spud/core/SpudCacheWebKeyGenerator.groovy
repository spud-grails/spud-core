package spud.core

import grails.plugin.cache.web.filter.DefaultWebKeyGenerator
import javax.servlet.http.HttpServletRequest;

class SpudCacheWebKeyGenerator extends DefaultWebKeyGenerator {
	public String generate(HttpServletRequest request) {
		String key = super.generate(request);

		return request.getServerName() + ':' + key;
	}
}
