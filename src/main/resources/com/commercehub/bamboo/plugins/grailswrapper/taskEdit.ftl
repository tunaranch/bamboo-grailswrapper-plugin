[@ww.textarea label="Grails commands" description="Each line will result in a separate invocation of the Grails wrapper.  Arguments can be included after the target name." name="commands" rows="5" cssClass="long-field" required="true"/]

[@ww.textfield label="Common options" description="These options will be prepended to each Grails command. Including '-non-interactive' is strongly suggested.  You can add multiple options separated by a space." name="commonOptions" cssClass="long-field" required="false"/>

[#assign addJdkLink][@ui.displayAddJdkInline /][/#assign]
[@ww.select cssClass="jdkSelectWidget"
            labelKey="builder.common.jdk" name="buildJdk"
            list=uiConfigSupport.jdkLabels required="true"
            extraUtility=addJdkLink/]

[@ww.textfield name="jvmOptions" label="JVM options" description="These options will be passed to the JVM via JAVA_OPTS.  Common examples include heap and perm gen settings.  You can add multiple options separated by a space." cssClass="long-field" required="false"/]

[@ww.textfield name="workingSubDirectory" label="Working sub directory" description="Specifies where to find the Grails wrapper, relative to the build working directory." cssClass="long-field" required="false"/]

[@ww.textarea name="environmentVariables" label="Environment variables" description="Treated as a Java properties file.  Each property will result in a separate environment variable." rows="5" cssClass="long-field" required="false"/]
