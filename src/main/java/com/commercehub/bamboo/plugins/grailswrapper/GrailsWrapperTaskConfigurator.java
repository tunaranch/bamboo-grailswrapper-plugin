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

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRequirementSupport;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.ww2.actions.build.admin.create.UIConfigSupport;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class GrailsWrapperTaskConfigurator extends AbstractTaskConfigurator implements TaskRequirementSupport {
    static final String COMMANDS = "commands";
    static final String COMMON_OPTIONS = "commonOptions";
    static final String JVM_OPTIONS = "jvmOptions";
    static final String ENVIRONMENT_VARIABLES = "environmentVariables";

    private static final String UI_CONFIG_SUPPORT = "uiConfigSupport";

    private static final Set<String> FIELD_SET = ImmutableSet.<String>builder().add(COMMANDS, COMMON_OPTIONS, JVM_OPTIONS, ENVIRONMENT_VARIABLES, TaskConfigConstants.CFG_JDK_LABEL, TaskConfigConstants.CFG_WORKING_SUB_DIRECTORY).build();

    private static final String DEFAULT_COMMANDS = "clean\ntest-app";
    private static final String DEFAULT_COMMON_OPTS = "-non-interactive -plain-output";

    private UIConfigSupport uiConfigSupport;

    @Override
    public void validate(@NotNull ActionParametersMap params, @NotNull ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        taskConfiguratorHelper.validateJdk(params, errorCollection);
        if (Strings.isNullOrEmpty(params.getString(COMMANDS))) {
            errorCollection.addError(COMMANDS, "At least one command must be provided.");
        }
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull ActionParametersMap params, @Nullable TaskDefinition previousTaskDefinition) {
        Map<String, String> taskConfig = super.generateTaskConfigMap(params, previousTaskDefinition);
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(taskConfig, params, FIELD_SET);
        return taskConfig;
    }

    @Override
    public void populateContextForCreate(@NotNull Map<String, Object> context) {
        super.populateContextForCreate(context);
        populateContextForAllOperations(context);
        context.put(COMMANDS, DEFAULT_COMMANDS);
        context.put(COMMON_OPTIONS, DEFAULT_COMMON_OPTS);
    }

    @Override
    public void populateContextForEdit(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        populateContextForAllOperations(context);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELD_SET);
    }

    @Override
    public void populateContextForView(@NotNull Map<String, Object> context, @NotNull TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);
        populateContextForAllOperations(context);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, FIELD_SET);
    }

    private void populateContextForAllOperations(@NotNull Map<String, Object> context) {
        context.put(UI_CONFIG_SUPPORT, uiConfigSupport);
    }

    /*
     * Called automatically by Bamboo
     */
    @SuppressWarnings("unused")
    public void setUiConfigSupport(UIConfigSupport uiConfigSupport) {
        this.uiConfigSupport = uiConfigSupport;
    }

    @NotNull
    @Override
    public Set<Requirement> calculateRequirements(@NotNull TaskDefinition taskDefinition) {
        Set<Requirement> requirements = Sets.newHashSet();
        taskConfiguratorHelper.addJdkRequirement(requirements, taskDefinition, TaskConfigConstants.CFG_JDK_LABEL);
        return requirements;
    }
}
