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

import ch.silviowangler.gradle.restapi.GeneratedSpecContainer
import ch.silviowangler.gradle.restapi.RestApiExtension
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.GradleException
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class GenerateRestApiTask extends SpecificationBaseTask {

	public static final String GET_COLLECTION = 'GET_COLLECTION'
	public static final String GET_ENTITY = 'GET_ENTITY'
	public static final String HEAD_COLLECTION = 'HEAD_COLLECTION'
	public static final String HEAD_ENTITY = 'HEAD_ENTITY'
	public static final String POST = 'POST'
	public static final String PUT = 'PUT'
	public static final String PUT_ENTITY = 'PUT_ENTITY'
	public static final String PUT_COLLECTION = 'PUT_COLLECTION'
	public static final String DELETE_ENTITY = 'DELETE_ENTITY'
	public static final String DELETE_COLLECTION = 'DELETE_COLLECTION'

	@OutputDirectory
	File getRootOutputDir() {
		project.restApi.generatorOutput
	}

	@TaskAction
	void exec() {

		if (!getRootOutputDir()) {
			throw new GradleException("generatorOutput dir must be set")
		}

		logger.lifecycle "Generating REST artifacts ..."
		long start = System.currentTimeMillis()
		int amountOfGeneratedJavaSourceFiles = 0
		final String fileSeparator = '/'

		RestApiExtension restApiExtension = project.restApi

		List<File> specs = findSpecifications(getOptionsSource())

		logger.lifecycle("Found ${specs.size()} specification files (${specs.collect { it.name}})")

		for (File specFile in specs) {

			println "Processing spec ${specFile.name}"
			GeneratedSpecContainer specContainer = specGenerator.generateJavaTypesForSpecification(specFile, project.restApi as RestApiExtension)

			for (TypeSpec model in specContainer.collectGeneratedTypes()) {
				amountOfGeneratedJavaSourceFiles++
				writeToFileSystem(specContainer.packageName, model, getRootOutputDir())
			}

			if (specContainer.restImplementation) {
				File file = new File(restApiExtension.generatorImplOutput, "${specContainer.packageName.replaceAll('\\.', fileSeparator)}${fileSeparator}${specContainer.restImplementation.name}.java")

				if (!file.exists()) {
					logger.lifecycle('Writing implementation {} to {}', file.name, restApiExtension.generatorImplOutput)
					amountOfGeneratedJavaSourceFiles++
					writeToFileSystem(specContainer.packageName, specContainer.restImplementation, restApiExtension.generatorImplOutput)
				} else {
					logger.lifecycle('Resource implementation {} exists. Skipping this one', file.name)
				}
			}
		}
		logger.lifecycle "Done generating REST artifacts in {} milliseconds. (Processed JSON {} files and generated {} Java source code files)", System.currentTimeMillis() - start, specs.size(), amountOfGeneratedJavaSourceFiles
	}

	private void writeToFileSystem(String packageName, TypeSpec typeSpec, File outputDir) {

		Objects.requireNonNull(packageName, "Package name must be present")
		Objects.requireNonNull(typeSpec, "Type spec must be present")
		Objects.requireNonNull(outputDir, "output dir must be present")

		if (!outputDir.canWrite()) {
			throw new IllegalStateException("I must have permission to write to ${outputDir.absolutePath}")
		}

		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).skipJavaLangImports(true).build()

		logger.info("Writing {} ...", typeSpec)
		if (isWriteToConsoleEnabled()) {
			javaFile.writeTo(System.out)
		}
		logger.debug('Writing to {}', outputDir.absolutePath)
		javaFile.writeTo(outputDir)
	}

	private boolean isWriteToConsoleEnabled() {
		getLogger().isDebugEnabled() || System.getProperty('silviowangler.rest-plugin.debug', 'false') != 'false'
	}
}
