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

import ch.silviowangler.gradle.restapi.GeneratorUtil
import ch.silviowangler.gradle.restapi.InputProcessingMode
import ch.silviowangler.gradle.restapi.ResourceFileComparator
import ch.silviowangler.gradle.restapi.builder.SpecGenerator
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal

/**
 * @author Silvio Wangler
 */
class SpecificationBaseTask extends DefaultTask {


	private final SpecGenerator specGenerator;

	SpecificationBaseTask() {
		this.specGenerator = new SpecGenerator()
	}

	@InputDirectory
	File getOptionsSource() {
		if (project.restApi.optionsSource) {
			return project.restApi.optionsSource
		}
		return new File(GeneratorUtil.generatorInput(project), "spec")
	}

	/**
	 * Finds all the specification files and return it.
	 * @param folder
	 * @return
	 */
	List<File> findSpecifications(File folder) {

		if (!folder.exists()) {
			throw new IllegalArgumentException("$folder.absolutePath does not exist")
		}

		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("$folder.absolutePath is not a directory")
		}

		List<File> specs = []
		if (project.restApi.inputProcessingMode == InputProcessingMode.RECURSIVE) {
			specs = findFilesRecursive(folder)
		} else {
			folder.eachFile(FileType.FILES, { f -> if (f.name.endsWith('.json')) specs << f })
			Collections.sort(specs, new ResourceFileComparator())
		}
		return specs
	}

	@Internal
	SpecGenerator getSpecGenerator() {
		return this.specGenerator;
	}

	private List<File> findFilesRecursive(File folder) {

		List<File> list = folder.listFiles(new FilenameFilter() {
					@Override
					boolean accept(File dir, String name) {
						return name.endsWith(".json")
					}
				})

		List<File> subfolders = folder.listFiles(new FilenameFilter() {
					@Override
					boolean accept(File dir, String name) {
						return dir.isDirectory()
					}
				})

		for (File subfolder in subfolders) {
			list.addAll findFilesRecursive(subfolder)
		}

		return !list ? []: list
	}
}
