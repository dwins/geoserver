.. _security:

Security
========

This section details the security subsystem in GeoServer, which is based on `Spring Security <http://static.springsource.org/spring-security/site/>`_. For web-based configuration, please see the section on :ref:`webadmin_security` in the :ref:`web_admin`.

As of GeoServer 2.2.0, the security subsystem has been completely re-engineered, providing a more secure and flexible authentication framework. This rework is largely based on a Christian Müeller's 
masters thesis entitled `Flexible Authentication for Stateless Web Services <http://geoserver.org/display/GEOS/Flexible+Authentication+for+Stateless+Web+Services>`_. It is good reading to help understanding many of the new concepts introduced. 

Overview
--------

When discussing security in GeoServer, it's important to have a clear understanding of the structure of the data being oecured.
Broadly, GeoServer manages two different categories of data - *configuration* and *geospatial data*.
The configuration is accessed through the GeoServer web administration console and GeoServer's REST API.
The security system allows administrators to constrain both the REST API itself (by :doc:`limiting certain URLs and HTTP methods</security/rest>` to particular users) and the configuration (by :doc:`granting or withholding "ADMIN" privileges</security/layer>` to particular users.)
Since GeoServer's only options for configuration security are checking whether the user has ADMIN or not for the requested operation, users without ADMIN access are still able to read the configuration.
(This is in part due to the fact that GeoServer must read the configuration in order to connect to a data source in the course of satisfying normal OGC service requests.)
However, in case sensitive information (such as database passwords) is in the configuration, it is possible to restrict direct access to the configuration by rejecting REST operations.
Conversely, a REST request that modifies data may be allowed at the level of the REST operation and still rejected if the user making it does not have ADMIN privileges for the resource in question.

The geospatial data is accessed through the OGC services that GeoServer implements, such as WMS, WFS, and WCS.
GeoServer's security system provides for rules that limit access at level of different operations (:doc:`OGC Services and service operations</security/service>` for OGC service access.)
It also allows controlling :doc:`READ and WRITE access for particular workspaces, layers, or stores</security/layer>` (please note that a user with ADMIN access to a resource's configuration also implicitly has both READ and WRITE access to the resource itself.  However ADMIN access is not required for *any* OGC service request.)
As with the REST constraints, an operation will succeed only if both the operation permissions and the data permissions allow it.
For example, if the WFS Insert operation is allowed for all users but a user who does not have WRITE access issues an Insert request, the request will be rejected.

When multiple rules apply to an operation for a particular user, the most permissive one is selected.
When multiple rules apply to a resource for a particular user, the most permissive one is selected.
Both the operation and the resource rules must allow a request in order for GeoServer to execute it.

.. toctree::
   :maxdepth: 2

   usergrouprole/index
   auth/index
   passwd
   root
   service
   layer
   rest
   disable
   tutorials/index
