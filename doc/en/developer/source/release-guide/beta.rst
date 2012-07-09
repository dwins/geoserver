.. _release_guide_beta:

Release Guide (Beta)
====================

This guide details the process of performing a GeoServer release.

.. note::

   This guide is still a work in progress. For now the official release guide
   remains to be :ref:`release_guide`.
   

Notify developer lists
----------------------

It is good practice to notify the `GeoServer developer list <https://lists.sourceforge.net/lists/listinfo/geoserver-devel>`_ of the intention to make the release a few days in advance, even though the release date has been agreed upon before hand. GeoServer releases are usually made in conjunction with GeoTools releases, so the GeoTools developer list should also be notified of the associated GeoTools release a few days in advance.


Prerequisites
-------------

The following are necessary to perform a GeoServer release:

#. Commit access to GeoServer `GIT <https://github.com/>`_
#. Edit access to the GeoServer Blog
#. Administration rights to the GeoServer bug tracker (JIRA)
#. Write access to GeoServer Maven Repository
#. release/file management privileges in sourceforge

If a parallel GeoTools release is being preformed, see the GeoTools Release Guide. Alternatively you can (nicely) ask one of the GeoTools developers to perform the release for you.

Versions and revisions
----------------------

Unlike in the past, we don't require developers to perform a code freeze as we will be releasing from Geotools and GeoServer version that have passed the CITE test.

To optain the GeoServer and Geotools GIT revision that have passed the `CITE test <http://hudson.opengeo.org/hudson/view/cite/>`_, navigate to the latest run of the CITE test on hudson and view it's console output and select to view its full log::

	example: http://hudson.opengeo.org/hudson/view/cite/job/cite-wfs-1.1-master/526/consoleFull
	
Perform a search on the log for 'git revision' and you should obtain the following.::

	version = 2.3-SNAPSHOT
	git revision = 68cbe445ca378e0dd04405870ef95c852189c1b0
	git branch = 68cbe445ca378e0dd04405870ef95c852189c1b0
	build date = 08-Jul-2012 03:36
	geotools version = 9-SNAPSHOT
	geotools revision = 9517eebf412ce5cda354e7945672293c37cddcc4

Since Geotools are normally release in parallel with GeoServer, it is advisable to use the Geotools version number such as 8.0 rather then its svn revision number 
when performing an official release

Build the Release
-----------------

#. Navigate to `GeoServer Hudson <http://hudson.opengeo.org/hudson>`_.
#. Run the ``geoserver-release`` job. The job takes the following parameters:

   * ``BRANCH`` - The branch to release from, "trunk", "2.1.x", etc...
   * ``REV`` - The subversion revision number to release from. If left blank the
     latest revision on the ``BRANCH`` being released is used.
   * ``VERSION`` - The version/name of the release to build, "2.1.4", "2.2",
     etc...
   * ``GT_VERSION`` - The GeoTools version to include in the release. This may 
     be specified as a version number such as "8.0" or "2.7.5". Alternatively 
     the version may be specified as a subversion branch/revision pair in the 
     form ``<branch>@<revision>``. For example "trunk@12345".
   * ``GWC_VERSION`` - The GeoWebCache version to include in the release. This
     may be specified as a version number such as "1.3-RC3". Alternatively the
     version may be specified as a git revision of the form 
     ``<branch>@<revision>`` such as "master@1b3243jb..."
   * ``SVN_TAG`` - A true/false parameter that specifies whether to actually 
     create the svn tag for the release. This is generally used for debugging
     purposes only. When set to "false" no tag will be created in subversion.
     

This job will checkout the specified branch/revision and build the GeoServer
release artifacts against the GeoTools/GeoWebCache versions specified. When 
successfully complete all release artifacts will be uploaded to the following
location::

   http://gridlock.opengeo.org/geoserver/release/<RELEASE> 
   
Additionally when the job completes it fires off two jobs for building the 
Windows and OSX installers. These jobs run on different hudson instances. 
When those jobs complete the ``.exe`` and ``.dmg`` artifacts will be uploaded
to the location referenced above.

Test the Artifacts
------------------

Download and try out some of the artifacts from the above location and do a 
quick smoke test that there are no issues. Engage other developers to help 
test on the developer list.

Release in JIRA
---------------

#. Navigate to `GeoServer Hudson <http://hudson.opengeo.org/hudson>`_.
#. Run the ``geoserver-release-jira`` job. The job takes the following
   parameters:

   * ``VERSION`` - The version to release, same as in the previous section. This 
     version must match a version in JIRA.
   * ``NEXT_VERSION`` - The next version in the series. All unresolved issues 
     currently fils against ``VERSION`` will be transitioned to this version.
   * ``JIRA_USER`` - A JIRA user name that has release privileges. This user 
     will be used to perform the release in JIRA, via the SOAP api.
   * ``JIRA_PASSWD`` - The password for the ``JIRA_USER``.
     
#. Navigate to `JIRA <http://jira.codehaus.org/browse/GEOS>`_ and verify that
   the version has actually been released.

This job will perform the tasks in JIRA to release ``VERSION``. 

Publish the Release
-------------------

#. Navigate to `GeoServer Hudson <http://hudson.opengeo.org/hudson>`_.
#. Run the ``geoserver-release-publish`` job. The job takes a single parameter 
   that is the ``VERSION`` specified above.
#. Navigate to `Sourceforge <http://sourceforge.net/projects/geoserver/>`_ and
   verify that the artifacts have been uploaded properly.
#. Set the necessary flags on the ``.exe``, ``.dmg`` and ``.bin`` artifacts so 
   that they show up as the appropriate default for users downloading on the 
   Windows, OSX, and Linux platforms.
   
This job will rsync all the artifacts located at::

   http://gridlock.opengeo.org/geoserver/release/<RELEASE>
   
to the SourceForge FRS server.

Announce the Release
--------------------

TODO: Take from the existing release guide.
