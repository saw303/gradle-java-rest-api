/*
 * MIT License
 *
 * Copyright (c) 2017 - 2017 Silvio Wangler (silvio.wangler@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
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
import org.gradle.api.artifacts.Configuration

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class RestApiPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = 'ch.silviowangler.restapi'


    void apply(Project project) {

        def resources = project.container(Resource)

        project.apply(plugin: 'java')

        def extension = new RestApiExtension(project)
        project.extensions.add('restApi', extension)

        project.task('cleanRestArtefacts', type: CleanRestApiTask, group: 'OSL Rest')
        project.task('extractSpecs', type: ExtractRestApiSpecsTask, group: 'OSL Rest')
        project.task('generateRestArtefacts', type: GenerateRestApiTask, group: 'OSL Rest')

        project.clean.dependsOn project.cleanRestArtefacts
        project.generateRestArtefacts.dependsOn project.extractSpecs
        project.compileJava.dependsOn project.generateRestArtefacts
        project.compileJava.options.encoding = 'UTF-8'
        project.compileTestJava.options.encoding = 'UTF-8'

        project.sourceSets.main.java.srcDir { project.osl.generatorOutput }

        Configuration oslSpecificationConfiguration = project.configurations.findByName('oslSpecification')

        if (!oslSpecificationConfiguration) {
            oslSpecificationConfiguration = project.configurations.create('oslSpecification')
        }
    }
}
