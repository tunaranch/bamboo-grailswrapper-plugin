# Bamboo Grails Wrapper Plugin

The Bamboo Grails Wrapper plugin facilitates easy use of the [Grails wrapper](http://grails.org/doc/2.3.x/guide/commandLine.html#wrapper) from within [Atlassian Bamboo](https://www.atlassian.com/software/bamboo).

# Pre-requisites

In order to use this plugin, you'll need an installed JDK, as well as a project that contains the Grails wrapper.

# Usage

After installing the plugin, you'll have a new "Grails Wrapper" task type available to add to build plans.  At a minimum, ensure that the `Grails commands` and `Build JDK` have the desired values.  If desired, you can specify additional Grails options, JVM options, or an alternate working directory.
