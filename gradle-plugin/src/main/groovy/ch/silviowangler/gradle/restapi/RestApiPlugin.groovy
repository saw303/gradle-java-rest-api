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
import ch.silviowangler.gradle.restapi.tasks.GenerateRestApiTask
import ch.silviowangler.gradle.restapi.tasks.PlantUmlTask
import ch.silviowangler.gradle.restapi.tasks.ValidationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.compile.JavaCompile

import static ch.silviowangler.gradle.restapi.Consts.CONFIGURATION_REST_API
import static ch.silviowangler.gradle.restapi.Consts.TASK_GROUP_REST_API
import static ch.silviowangler.gradle.restapi.TargetFramework.MICRONAUT
import static ch.silviowangler.gradle.restapi.TargetFramework.SPRING_BOOT

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class RestApiPlugin implements Plugin<Project> {

	public static final String PLUGIN_ID = 'ch.silviowangler.restapi'


	void apply(Project project) {

		project.apply(plugin: 'java-library')

		RestApiExtension extension = new RestApiExtension(project)
		project.extensions.add('restApi', extension)


		def clean = project.tasks.register('cleanRestArtifacts', CleanRestApiTask) { CleanRestApiTask t ->
			t.group = TASK_GROUP_REST_API
		}
		def extract = project.tasks.register('extractSpecs', ExtractRestApiSpecsTask) { ExtractRestApiSpecsTask t ->
			t.group = TASK_GROUP_REST_API
		}
		def validate = project.tasks.register('validateRestSpecs', ValidationTask) { ValidationTask t ->
			t.group = TASK_GROUP_REST_API
		}
		def generate = project.tasks.register('generateRestArtifacts', GenerateRestApiTask) { GenerateRestApiTask t ->
			t.group = TASK_GROUP_REST_API
			t.dependsOn(validate)
		}
		def generateDiagrams = project.tasks.register('generateDiagrams', PlantUmlTask) { PlantUmlTask t ->
			t.group = TASK_GROUP_REST_API
		}

		project.tasks.named('clean').configure {
			dependsOn(clean)
		}

		project.tasks.named('compileJava').configure {
			dependsOn(generate)
			options.encoding = 'UTF-8'
		}

		project.tasks.named('compileTestJava').configure {
			options.encoding = 'UTF-8'
		}

		project.sourceSets.main.java.srcDir { project.restApi.generatorOutput }

		Configuration restApiSpecification = project.configurations.findByName(CONFIGURATION_REST_API)

		if (!restApiSpecification) {
			project.configurations.create(CONFIGURATION_REST_API)
		}

		final String springVersion = "5.2.4.RELEASE"
		final String pluginVersion = "2.2.7"
		final String libPhoneNumberVersion = "8.11.5"

		project.afterEvaluate {

			project.dependencies {

				if (extension.generationMode == GenerationMode.API) {
					api "javax.annotation:javax.annotation-api:1.3.2"
					api "ch.silviowangler.rest:rest-model:${pluginVersion}"
					api "javax.money:money-api:1.0.3"
					api "javax.validation:validation-api:2.0.1.Final"
				}
				else {
					implementation "javax.annotation:javax.annotation-api:1.3.2"
					implementation "ch.silviowangler.rest:rest-model:${pluginVersion}"
					implementation "javax.money:money-api:1.0.3"
					implementation "javax.validation:validation-api:2.0.1.Final"
				}
				implementation "com.googlecode.libphonenumber:libphonenumber:${libPhoneNumberVersion}"

				if (extension.generationMode != GenerationMode.API) {

					if (extension.targetFramework == SPRING_BOOT) {
						implementation "ch.silviowangler.rest:rest-api-spring:${pluginVersion}"
						compileOnly "org.springframework:spring-web:${springVersion}"
						compileOnly "org.springframework:spring-webmvc:${springVersion}"
					} else if (extension.targetFramework == MICRONAUT) {
						implementation "ch.silviowangler.rest:rest-api-micronaut:${pluginVersion}"
					}
				}
			}
		}
	}
}
