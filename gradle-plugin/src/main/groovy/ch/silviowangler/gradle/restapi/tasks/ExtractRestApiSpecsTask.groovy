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
package ch.silviowangler.gradle.restapi.tasks

import ch.silviowangler.gradle.restapi.Consts
import ch.silviowangler.gradle.restapi.GeneratorUtil
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class ExtractRestApiSpecsTask extends DefaultTask {

	File extractOutputDir

	ExtractRestApiSpecsTask() {
		this.extractOutputDir = GeneratorUtil.generatorInput(project)
	}

	@OutputDirectory
	File getExtractOutputDir() {
		return extractOutputDir
	}

	@TaskAction
	void extract() {
		if (isConfigurationRestApiDefined(project)) {

			Configuration configuration = project.configurations.findByName(Consts.CONFIGURATION_REST_API)
			Set<File> files = configuration.files

			for (File file in files) {
				project.copy {
					from project.zipTree(file)
					into this.extractOutputDir
				}
			}
		}
	}

	static boolean isConfigurationRestApiDefined(Project project) {
		Configuration configuration = project.configurations.findByName(Consts.CONFIGURATION_REST_API)
		return configuration && !configuration.files.isEmpty()
	}
}
