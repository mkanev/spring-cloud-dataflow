/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.server.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.admin.service.JobService;
import org.springframework.boot.actuate.metrics.repository.MetricRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.dataflow.completion.StreamCompletionProvider;
import org.springframework.cloud.dataflow.registry.AppRegistry;
import org.springframework.cloud.dataflow.registry.DataFlowUriRegistryPopulator;
import org.springframework.cloud.dataflow.registry.DataFlowUriRegistryPopulatorProperties;
import org.springframework.cloud.dataflow.registry.RedisUriRegistry;
import org.springframework.cloud.dataflow.server.controller.AggregateCounterController;
import org.springframework.cloud.dataflow.server.controller.AppRegistryController;
import org.springframework.cloud.dataflow.server.controller.CompletionController;
import org.springframework.cloud.dataflow.server.controller.CounterController;
import org.springframework.cloud.dataflow.server.controller.FieldValueCounterController;
import org.springframework.cloud.dataflow.server.controller.JobExecutionController;
import org.springframework.cloud.dataflow.server.controller.JobInstanceController;
import org.springframework.cloud.dataflow.server.controller.JobStepExecutionController;
import org.springframework.cloud.dataflow.server.controller.JobStepExecutionProgressController;
import org.springframework.cloud.dataflow.server.controller.RestControllerAdvice;
import org.springframework.cloud.dataflow.server.controller.RootController;
import org.springframework.cloud.dataflow.server.controller.RuntimeAppsController;
import org.springframework.cloud.dataflow.server.controller.RuntimeAppsController.AppInstanceController;
import org.springframework.cloud.dataflow.server.controller.StreamDefinitionController;
import org.springframework.cloud.dataflow.server.controller.StreamDeploymentController;
import org.springframework.cloud.dataflow.server.controller.TaskDefinitionController;
import org.springframework.cloud.dataflow.server.controller.TaskDeploymentController;
import org.springframework.cloud.dataflow.server.controller.TaskExecutionController;
import org.springframework.cloud.dataflow.server.controller.UiController;
import org.springframework.cloud.dataflow.server.controller.security.SecurityController;
import org.springframework.cloud.dataflow.server.repository.DeploymentIdRepository;
import org.springframework.cloud.dataflow.server.repository.StreamDefinitionRepository;
import org.springframework.cloud.dataflow.server.repository.TaskDefinitionRepository;
import org.springframework.cloud.dataflow.server.service.TaskJobService;
import org.springframework.cloud.dataflow.server.service.TaskService;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.resource.maven.MavenResourceLoader;
import org.springframework.cloud.deployer.resource.registry.UriRegistry;
import org.springframework.cloud.deployer.resource.registry.UriRegistryPopulator;
import org.springframework.cloud.deployer.resource.support.DelegatingResourceLoader;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.stream.app.metrics.AggregateCounterRepository;
import org.springframework.cloud.stream.app.metrics.FieldValueCounterRepository;
import org.springframework.cloud.stream.configuration.metadata.ApplicationConfigurationMetadataResolver;
import org.springframework.cloud.task.repository.TaskExplorer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.hateoas.EntityLinks;

/**
 * Configuration for the Data Flow Server Controllers.
 *
 * @author Mark Fisher
 * @author Gunnar Hillert
 */
@SuppressWarnings("ALL")
@Configuration
@ConditionalOnBean({ AppDeployer.class, TaskLauncher.class })
@EnableConfigurationProperties(DataFlowUriRegistryPopulatorProperties.class)
public class DataFlowControllerAutoConfiguration {

	@Bean
	public UriRegistry uriRegistry(RedisConnectionFactory connectionFactory) {
		return new RedisUriRegistry(connectionFactory);
	}

	@Bean
	public UriRegistryPopulator uriRegistryPopulator() {
		return new UriRegistryPopulator();
	}

	@Bean
	public AppRegistry appRegistry(UriRegistry uriRegistry, DelegatingResourceLoader resourceLoader) {
		return new AppRegistry(uriRegistry, resourceLoader);
	}

	@Bean
	public DataFlowUriRegistryPopulator dataflowUriRegistryPopulator(UriRegistry uriRegistry,
			DataFlowUriRegistryPopulatorProperties properties) {
		return new DataFlowUriRegistryPopulator(uriRegistry, uriRegistryPopulator(), properties);
	}

	@Bean
	public RootController rootController(EntityLinks entityLinks) {
		return new RootController(entityLinks);
	}

	@Bean
	public RuntimeAppsController runtimeAppsController(StreamDefinitionRepository repository,
			DeploymentIdRepository deploymentIdRepository, AppDeployer appDeployer) {
		return new RuntimeAppsController(repository, deploymentIdRepository, appDeployer);
	}

	@Bean
	public AppInstanceController appInstanceController(AppDeployer appDeployer) {
		return new AppInstanceController(appDeployer);
	}

	@Bean
	public StreamDefinitionController streamDefinitionController(StreamDefinitionRepository repository,
			DeploymentIdRepository deploymentIdRepository, StreamDeploymentController deploymentController,
			AppDeployer deployer, AppRegistry appRegistry) {
		return new StreamDefinitionController(repository, deploymentIdRepository, deploymentController, deployer,
				appRegistry);
	}

	@Bean
	public StreamDeploymentController streamDeploymentController(StreamDefinitionRepository repository,
			DeploymentIdRepository deploymentIdRepository, AppRegistry registry, AppDeployer deployer) {
		return new StreamDeploymentController(repository, deploymentIdRepository, registry, deployer);
	}

	@Bean
	public MavenResourceLoader mavenResourceLoader(MavenProperties properties) {
		return new MavenResourceLoader(properties);
	}

	@Bean
	@ConditionalOnMissingBean(DelegatingResourceLoader.class)
	public DelegatingResourceLoader delegatingResourceLoader(MavenResourceLoader mavenResourceLoader) {
		Map<String, ResourceLoader> loaders = new HashMap<>();
		loaders.put("maven", mavenResourceLoader);
		return new DelegatingResourceLoader(loaders);
	}

	@Bean
	public TaskDefinitionController taskDefinitionController(TaskDefinitionRepository repository,
			DeploymentIdRepository deploymentIdRepository, TaskLauncher taskLauncher) {
		return new TaskDefinitionController(repository, deploymentIdRepository, taskLauncher);
	}

	@Bean
	public TaskDeploymentController taskDeploymentController(TaskService taskService) {
		return new TaskDeploymentController(taskService);
	}

	@Bean
	public TaskExecutionController taskExecutionController(TaskExplorer explorer) {
		return new TaskExecutionController(explorer);
	}

	@Bean
	public JobExecutionController jobExecutionController(TaskJobService repository) {
		return new JobExecutionController(repository);
	}

	@Bean
	public JobStepExecutionController jobStepExecutionController(JobService service) {
		return new JobStepExecutionController(service);
	}

	@Bean
	public JobStepExecutionProgressController jobStepExecutionProgressController(JobService service) {
		return new JobStepExecutionProgressController(service);
	}

	@Bean
	public JobInstanceController jobInstanceController(TaskJobService repository) {
		return new JobInstanceController(repository);
	}

	@Bean
	public CounterController counterController(MetricRepository metricRepository) {
		return new CounterController(metricRepository);
	}

	@Bean
	public CompletionController completionController(StreamCompletionProvider completionProvider) {
		return new CompletionController(completionProvider);
	}

	@Bean
	public FieldValueCounterController fieldValueCounterController(FieldValueCounterRepository repository) {
		return new FieldValueCounterController(repository);
	}

	@Bean
	public AppRegistryController appRegistryController(AppRegistry appRegistry, ApplicationConfigurationMetadataResolver metadataResolver) {
		return new AppRegistryController(appRegistry, metadataResolver);
	}

	@Bean
	public AggregateCounterController aggregateCounterController(AggregateCounterRepository repository) {
		return new AggregateCounterController(repository);
	}

	@Bean
	public SecurityController securityController(SecurityProperties securityProperties) {
		return new SecurityController(securityProperties);
	}

	@Bean
	public UiController uiController() {
		return new UiController();
	}

	@Bean
	public RestControllerAdvice restControllerAdvice() {
		return new RestControllerAdvice();
	}

	@Bean
	public MavenProperties mavenProperties() {
		return new MavenConfigurationProperties();
	}

	@ConfigurationProperties(prefix = "maven")
	static class MavenConfigurationProperties extends MavenProperties {
	}
}
