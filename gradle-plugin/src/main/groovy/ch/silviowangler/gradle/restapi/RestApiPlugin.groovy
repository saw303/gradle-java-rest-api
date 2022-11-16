/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2020 Silvio Wangler (silvio.wangler@gmail.com)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ch.silviowangler.gradle.restapi

import ch.silviowangler.gradle.restapi.tasks.CleanRestApiTask
import ch.silviowangler.gradle.restapi.tasks.ExtractRestApiSpecsTask
import ch.silviowangler.gradle.restapi.tasks.GenerateRestApiAsciiDocTask
import ch.silviowangler.gradle.restapi.tasks.GenerateRestApiTask
import ch.silviowangler.gradle.restapi.tasks.PlantUmlTask
import ch.silviowangler.gradle.restapi.tasks.ValidationTask
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile

import static ch.silviowangler.gradle.restapi.Consts.CONFIGURATION_REST_API
import static ch.silviowangler.gradle.restapi.Consts.TASK_GROUP_REST_API
import static ch.silviowangler.gradle.restapi.TargetFramework.SPRING_BOOT

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class RestApiPlugin implements Plugin<Project> {

	public static final String PLUGIN_ID = 'io.github.saw303.restapi'


	void apply(Project project) {

		project.apply(plugin: 'java-library')

		RestApiExtension extension = project.extensions.create('restApi', RestApiExtension)
		Provider<TargetFramework> tf = project.provider { extension.targetFramework }
		Provider<GenerationMode> generationMode = project.provider { extension.generationMode }

		project.afterEvaluate {
			println "Generating rest api for project ${project.name} using target framework ${tf.get().name()}"
		}

		TaskProvider<CleanRestApiTask> clean = project.tasks.register('cleanRestArtifacts', CleanRestApiTask) { CleanRestApiTask t ->
			t.group = TASK_GROUP_REST_API
		}

		TaskProvider<ExtractRestApiSpecsTask> extract = project.tasks.register('extractSpecs', ExtractRestApiSpecsTask) { ExtractRestApiSpecsTask t ->
			t.group = TASK_GROUP_REST_API
		}

		TaskProvider<ValidationTask> validate = project.tasks.register('validateRestSpecs', ValidationTask) { ValidationTask t ->
			t.group = TASK_GROUP_REST_API
			if (ExtractRestApiSpecsTask.isConfigurationRestApiDefined(project)) {
				t.dependsOn(extract)
			}
		}

		TaskProvider<GenerateRestApiTask> generate = project.tasks.register('generateRestArtifacts', GenerateRestApiTask) { GenerateRestApiTask t ->
			t.group = TASK_GROUP_REST_API
			t.dependsOn(validate)
		}

		project.tasks.register('generateDiagrams', PlantUmlTask) { PlantUmlTask t ->
			t.group = TASK_GROUP_REST_API
		}

		project.tasks.register('generateAsciiDocs', GenerateRestApiAsciiDocTask) { GenerateRestApiAsciiDocTask t ->
			t.group = TASK_GROUP_REST_API
		}

		project.tasks.named('clean').configure {
			dependsOn(clean)
		}

		project.tasks.named('compileJava').configure { JavaCompile task ->
			task.dependsOn(generate)
			task.options.encoding = 'UTF-8'
		}

		project.tasks.named('compileTestJava').configure { JavaCompile task ->
			task.options.encoding = 'UTF-8'
		}

		project.sourceSets.main.java.srcDir { project.restApi.generatorOutput }

		project.configurations.maybeCreate(CONFIGURATION_REST_API)

		final String springVersion = "5.2.4.RELEASE"
		final String pluginVersion = "3.0.7"
		final String libPhoneNumberVersion = "8.11.5"

		final List<String> deps = [
			"javax.annotation:javax.annotation-api:1.3.2",
			"ch.silviowangler.rest:rest-model:${pluginVersion}",
			"javax.money:money-api:1.0.3",
			"javax.validation:validation-api:2.0.1.Final"
		]

		NamedDomainObjectProvider<Configuration> api = project.configurations.named("api")
		NamedDomainObjectProvider<Configuration> implementation = project.configurations.named("implementation")
		NamedDomainObjectProvider<Configuration> compileOnly = project.configurations.named("compileOnly")

		api.configure { a ->
			a.withDependencies {
				if (generationMode.get().isApiCodeGenerationRequired() || generationMode.get().isClientCodeGenerationRequired()) {
					deps.each { dep -> it.add(project.dependencies.create(dep)) }
				}
			}
		}

		implementation.configure { impl ->
			impl.withDependencies {

				if (! generationMode.get().isApiCodeGenerationRequired() && !generationMode.get().isClientCodeGenerationRequired()) {
					deps.each { dep -> it.add(project.dependencies.create(dep)) }
				}

				it.add(project.dependencies.create("com.googlecode.libphonenumber:libphonenumber:${libPhoneNumberVersion}"))

				if (generationMode.get() != GenerationMode.API) {
					if (tf.get() == SPRING_BOOT) {
						it.add(project.dependencies.create("ch.silviowangler.rest:rest-api-spring:${pluginVersion}"))
					} else if (tf.get().isMicronaut()) {
						it.add(project.dependencies.create("ch.silviowangler.rest:rest-api-micronaut:${pluginVersion}"))
					}
				}
			}
		}

		compileOnly.configure { cO ->
			cO.withDependencies {
				if (tf.get() == SPRING_BOOT) {
					it.add(project.dependencies.create("org.springframework:spring-web:${springVersion}"))
					it.add(project.dependencies.create("org.springframework:spring-webmvc:${springVersion}"))
				}
			}
		}
	}
}
