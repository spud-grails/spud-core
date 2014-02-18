Spud Core Admin
===============

Spud Admin is a dependency package that adds a nice looking administrative panel to any project you add it to. It supports easy grails app integration and there are several planned future engines that we plan on designing for the spud suite. The first of which is Spud CMS which is being worked on now. This plugin will provide several common interfaces for the spud suite of tools that can also be used independently:

* Template Renderer Abstraction
* Wysiwyg editor support and extensible formatter support.
* Clean administrative interface
* Admin Controller Annotations
* Multisite mode (In the future)

Installation/Usage
------------------

Simply add spud core to your plugins

```groovy
plugins {
	compile ':spud-core:0.1.0'
}
```

Now you have a nifty clean administrative panel mapped to the "/spud/admin" url mapping (give or take a contextPath).

Security
--------

By default this plugin takes advantage of the [grails-security-bridge](http://grails.org/plugin/security-bridge) plugin, which provides a clean decoupled security interface. Dropping this plugin into your existing application security is as simple as implementing this plugin interface. An alternative option is to use the `spud-security` plugin which should be released shortly and contains a full security implementation via spring-security-core.

Sitemaps
--------

This plugin comes packaged with the [grails sitemaps plugin](http://grails.org/plugin/sitemaps). This plugin creates a clean artefact based interface for building out sitemaps. Please refer to this plugins documentation for more information on how to build sitemaps.

Adding your own Admin Panels
-----------------------------

Creating a grails application/controller that ties into spud admin is fairly straight forward. Using the power of annotations, controllers can be registered as an administrative module:

```groovy
package spud.admin
import  spud.core.*

@SpudApp(name="Users", thumbnail="spud/admin/users_thumb.png", order="99")
@SpudSecure(['USERS'])
class UserController {
  static namespace = 'spud_admin'


  def index = {
    def users = SpudUser.list([max:25] + params)
      render view: '/spud/admin/users/index', model:[users: users, userCount: SpudUser.count()]
  }
}
```

The example above uses the `@SpudApp` annotation to define the controller as a Users admin application. This will be displayed on the administrative dashboard for user management.

You can use the layouts provided with spud admin by using 'spud/admin/application' or 'spud/admin/detail' layouts

When creating controllers for the admin panel create them in the spud.admin classpath and preferably use `resource` REST style UrlMappings

NOTE: Spud Core is Retina Resolution Compatible Now

License
-------
APACHE 2.0
