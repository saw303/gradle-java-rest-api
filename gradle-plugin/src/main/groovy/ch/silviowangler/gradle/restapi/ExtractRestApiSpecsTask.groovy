/*
 * MIT License
 *
 * Copyright (c) 2016 - 2018 Silvio Wangler (silvio.wangler@gmail.com)
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

import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class ExtractRestApiSpecsTask extends AbstractTask {

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

        Configuration configuration = project.configurations.findByName(Consts.CONFIGUATION_REST_API)

        if (configuration && !configuration.files.isEmpty()) {

            Set<File> files = configuration.files

            for (File file in files) {
                project.copy {
                    from project.zipTree(file)
                    into this.extractOutputDir
                }
            }
        }
    }
}
