/*
 * Copyright 2013 CommerceHub and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.commercehub.bamboo.plugins.grailswrapper;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.CommandlineStringUtils;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.ExternalProcessBuilder;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.v2.build.agent.capability.Capability;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilityContext;
import com.atlassian.bamboo.v2.build.agent.capability.ReadOnlyCapabilitySet;
import com.atlassian.utils.process.ExternalProcess;
import com.atlassian.utils.process.ProcessHandler;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

public class GrailsWrapperTask implements TaskType {
    private static final String JDK_LABEL_KEY = "system.jdk.";

    private final ProcessService processService;
    private final EnvironmentVariableAccessor environmentVariableAccessor;
    private final CapabilityContext capabilityContext;

    public GrailsWrapperTask(ProcessService processService, EnvironmentVariableAccessor environmentVariableAccessor, CapabilityContext capabilityContext) {
        this.processService = processService;
        this.environmentVariableAccessor = environmentVariableAccessor;
        this.capabilityContext = capabilityContext;
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull TaskContext taskContext) throws TaskException {
        TaskResultBuilder taskResultBuilder = TaskResultBuilder.newBuilder(taskContext);
        BuildLogger buildLogger = taskContext.getBuildLogger();
        Map<String, String> environment = buildEnvironment(taskContext);
        File workingDirectory = taskContext.getWorkingDirectory();
        if (!workingDirectory.isDirectory()) {
            buildLogger.addErrorLogEntry("Working directory " + workingDirectory.getPath() + " does not exist.");
            return taskResultBuilder.failedWithError().build();
        }
        String wrapperExecutable = SystemUtils.IS_OS_WINDOWS ? "grailsw.bat" : "./grailsw";
        File wrapperExecutableFile = new File(workingDirectory, wrapperExecutable);
        if (!wrapperExecutableFile.isFile()) {
            buildLogger.addErrorLogEntry("Could not locate " + wrapperExecutable + " in working directory " + workingDirectory.getPath());
            return taskResultBuilder.failedWithError().build();
        }
        if (!wrapperExecutableFile.canExecute()) {
            buildLogger.addErrorLogEntry(wrapperExecutable + " in working directory " + workingDirectory.getPath() + " is not executable");
            return taskResultBuilder.failedWithError().build();
        }
        Joiner commandJoiner = Joiner.on(" ");
        for (List<String> command : parseCommands(taskContext)) {
            command.add(0, wrapperExecutable);
            ExternalProcessBuilder processBuilder = new ExternalProcessBuilder()
                    .workingDirectory(workingDirectory).env(environment).command(command);
            ExternalProcess process = processService.executeExternalProcess(taskContext, processBuilder);
            taskResultBuilder.checkReturnCode(process);
            ProcessHandler handler = process.getHandler();
            if (!handler.succeeded() || handler.getExitCode() != 0) {
                buildLogger.addErrorLogEntry("Grails wrapper command '" + commandJoiner.join(command) + "' failed; skipping any subsequent commands");
                break;
            }
        }
        return taskResultBuilder.build();
    }

    @NotNull
    private Map<String, String> buildEnvironment(@NotNull TaskContext taskContext) {
        ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        Map<String, String> environment = Maps.newHashMap(environmentVariableAccessor.getEnvironment(taskContext));
        String javaHome = getJavaHome(taskContext);
        String javaOpts = Strings.emptyToNull(configurationMap.get(GrailsWrapperTaskConfigurator.JVM_OPTIONS));
        if (javaHome != null) {
            environment.put("JAVA_HOME", javaHome);
        }
        if (javaOpts != null) {
            environment.put("JAVA_OPTS", javaOpts);
        }
        return environment;
    }

    @NotNull
    private Iterable<List<String>> parseCommands(@NotNull TaskContext taskContext) {
        ConfigurationMap configurationMap = taskContext.getConfigurationMap();
        String rawCommands = configurationMap.get(GrailsWrapperTaskConfigurator.COMMANDS);
        String rawCommonOpts = configurationMap.get(GrailsWrapperTaskConfigurator.COMMON_OPTIONS);
        String commonOpts = Utils.ensureEndsWith(Strings.nullToEmpty(rawCommonOpts), " ");
        Splitter splitter = Splitter.onPattern("[\r\n]").trimResults().omitEmptyStrings();
        Iterable<String> splitCommands = splitter.split(rawCommands);
        Function<String, String> commonOptsFunction = new StringPrependFunction(commonOpts);
        Function<String, List<String>> tokenizeFunction = CommandlineStringUtils.tokeniseCommandlineFunction();
        return Iterables.transform(splitCommands, Functions.compose(tokenizeFunction, commonOptsFunction));
    }

    @Nullable
    private String getJavaHome(@NotNull TaskContext taskContext) {
        String javaHome = null;
        ReadOnlyCapabilitySet capabilitySet = capabilityContext.getCapabilitySet();
        if (capabilitySet != null) {
            String jdkLabel = taskContext.getConfigurationMap().get(TaskConfigConstants.CFG_JDK_LABEL);
            Capability capability = capabilitySet.getCapability(JDK_LABEL_KEY + jdkLabel);
            if (capability != null) {
                javaHome = Strings.emptyToNull(capability.getValue());
            }
        }
        return javaHome;
    }
}
