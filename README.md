# Bamboo Grails Wrapper Plugin

The Bamboo Grails Wrapper plugin facilitates easy use of the [Grails wrapper](http://grails.org/doc/2.3.x/guide/commandLine.html#wrapper) from within [Atlassian Bamboo](https://www.atlassian.com/software/bamboo).

# Pre-requisites

In order to use this plugin, you'll need an installed JDK, as well as a project that contains the Grails wrapper.

# Usage

After installing the plugin, you'll have a new "Grails Wrapper" task type available to add to build plans.  At a minimum, ensure that the `Grails commands` and `Build JDK` have the desired values.  If desired, you can specify additional Grails options, JVM options, or an alternate working directory.

# Building

The officially recommended way to develop plugins for Atlassian applications is to install the Atlassian Plugin SDK.  You can find information about it in the [SDK Development documentation](https://developer.atlassian.com/display/DOCS/Getting+Started).

Alternatively, you should be able to build this plugin with just [Maven](http://maven.apache.org/).  However, it doesn't appear that the Atlassian plugins are compatible with all versions of Maven.  As of this writing, it appears to work with version 3.0.5.
