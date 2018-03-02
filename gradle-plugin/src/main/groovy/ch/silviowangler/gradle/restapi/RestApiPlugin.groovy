/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2018 Silvio Wangler (silvio.wangler@gmail.com)
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

import static ch.silviowangler.gradle.restapi.Consts.CONFIGUATION_REST_API
import static ch.silviowangler.gradle.restapi.Consts.TASK_GROUP_REST_API

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class RestApiPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = 'ch.silviowangler.restapi'


    void apply(Project project) {

        project.apply(plugin: 'java')

        def extension = new RestApiExtension(project)
        project.extensions.add('restApi', extension)


        Task clean = project.task('cleanRestArtifacts', type: CleanRestApiTask, group: TASK_GROUP_REST_API)
        Task extract = project.task('extractSpecs', type: ExtractRestApiSpecsTask, group: TASK_GROUP_REST_API)
        Task generate = project.task('generateRestArtifacts', type: GenerateRestApiTask, group: TASK_GROUP_REST_API)

        project.clean.dependsOn clean
        extract.dependsOn extract
        project.compileJava.dependsOn generate

        project.compileJava.options.encoding = 'UTF-8'
        project.compileTestJava.options.encoding = 'UTF-8'

        project.sourceSets.main.java.srcDir { project.restApi.generatorOutput }

        Configuration restApiSpecification = project.configurations.findByName(CONFIGUATION_REST_API)

        if (!restApiSpecification) {
            project.configurations.create(CONFIGUATION_REST_API)
        }

        project.dependencies {
            compile("ch.silviowangler.rest:rest-api-spring:1.0.14")
            compile("org.javamoney:moneta:1.1")
        }
    }
}
