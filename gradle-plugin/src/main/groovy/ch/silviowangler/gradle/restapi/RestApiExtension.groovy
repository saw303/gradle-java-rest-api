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

import ch.silviowangler.gradle.restapi.tasks.ExtractRestApiSpecsTask
import org.gradle.api.Project

import java.nio.charset.Charset

/**
 * @author Silvio Wangler
 */
class RestApiExtension {

	Project project
	String packageName
	File optionsSource
	InputProcessingMode inputProcessingMode = InputProcessingMode.FLATDIR
	File generatorOutput = project.file("${project.buildDir}/src/generated/java")
	File generatorImplOutput = project.file("${project.projectDir}/src/main/java")
	Closure objectResourceModelMapping = { resource, field -> throw new RuntimeException("No object resource model mapping for field ${field.name} and resource ${description}") }
	boolean generateDateAttribute = true
	boolean enableSecurity = false
	TargetFramework targetFramework = TargetFramework.JAX_RS
	Charset responseEncoding
	File diagramOutput = new File(project.buildDir, 'diagrams')
	File asciiDocOutput = new File(project.buildDir, 'asciiDocGenerated')
	boolean diagramShowFields = false
	GenerationMode generationMode = GenerationMode.ALL
	String clientId = "osl"

	RestApiExtension(Project project) {
		this.project = project
	}

	void setGenerationMode(GenerationMode generationMode) {
		if (generationMode == null) {
			throw new IllegalArgumentException("generationMode must not be null")
		}
		this.generationMode = generationMode
	}

	void setSubresourceConversion(Map<String, String> subresourceConversion) {
		this.subresourceConversion = subresourceConversion
	}

	void setObjectResourceModelMapping(Closure objectResourceModelMapping) {
		this.objectResourceModelMapping = objectResourceModelMapping
	}

	void setInputProcessingMode(InputProcessingMode inputProcessingMode) {
		this.inputProcessingMode = inputProcessingMode
	}

	void setOptionsSource(File optionsSource) {
		if (!optionsSource?.isDirectory() && !ExtractRestApiSpecsTask.isConfigurationRestApiDefined(project)) {
			throw new IllegalArgumentException("optionsSource '${optionsSource.absolutePath}' must be a directory")
		}
		this.optionsSource = optionsSource
	}

	void setGeneratorOutput(File generatorOutput) {

		if (!generatorOutput.exists()) {
			generatorOutput.mkdirs()
		}

		if (!generatorOutput?.isDirectory()) {
			throw new IllegalArgumentException("generatorOutput must be a directory")
		}

		this.generatorOutput = generatorOutput
	}

	void setGeneratorImplOutput(File generatorImplOutput) {

		if (!generatorImplOutput?.isDirectory()) {
			throw new IllegalArgumentException("generatorImplOutput must be a directory")
		}

		this.generatorImplOutput = generatorImplOutput
	}

	void setDiagramOutput(File diagramOutput) {
		if (!diagramOutput?.isDirectory()) {
			throw new IllegalArgumentException("diagramOutput must be a directory")
		}
		this.diagramOutput = diagramOutput
	}
}
