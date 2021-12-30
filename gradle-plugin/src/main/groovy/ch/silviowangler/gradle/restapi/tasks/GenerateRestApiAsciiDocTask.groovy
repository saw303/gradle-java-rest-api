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

import ch.silviowangler.gradle.restapi.asciidoc.AsciiDocFieldsAssistant
import ch.silviowangler.gradle.restapi.builder.ResourceContractContainer
import groovy.text.SimpleTemplateEngine
import org.apache.commons.lang3.StringUtils
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * @author Osian Hughes
 */
class GenerateRestApiAsciiDocTask extends SpecificationBaseTask {

	@OutputDirectory
	File getRootOutputDir() {
		project.restApi.asciiDocOutput
	}

	@TaskAction
	void generateAsciiDoc() {
		SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
		List<File> specs = findSpecifications(getOptionsSource())
		List<ResourceContractContainer> contracts = []
		specs.each { File specFile -> contracts << specGenerator.parseResourceContract(specFile) }

		String resource = '/asciidoc_templates/index.asciidoc.template'
		URL url = getClass().getResource(resource)
		def template = templateEngine.createTemplate(url).make([containers: contracts])

		getRootOutputDir().listFiles().collect {f -> f.delete()}

		File targetFile = new File(getRootOutputDir(), 'index.adoc')
		targetFile.createNewFile()
		targetFile.write(template.toString(), 'UTF-8')

		println "Writing ASCIIDoc file: ${targetFile.absolutePath}"

		resource = '/asciidoc_templates/container_details.asciidoc.template'
		url = getClass().getResource(resource)
		contracts.collect {container ->

			String[] title = StringUtils.splitByCharacterTypeCamelCase(container.resourceContract.general.name)
			title[0] = StringUtils.capitalize(title[0])
			def vars = [
				'title': StringUtils.join(title, ' '),
				'version': container.resourceContract.general.version,
				'description': container.resourceContract.general.description,
				'endpoint': container.resourceContract.general.xRoute,
				'verbs': container.resourceContract.verbs,
				'fieldAssistant': new AsciiDocFieldsAssistant(container.resourceContract.fields),
				'subResources': container.resourceContract.subresources]

			template = templateEngine.createTemplate(url).make(vars)

			targetFile = new File(getRootOutputDir(), "${container.resourceContract.general.name}.adoc")
			targetFile.createNewFile()
			targetFile.write(template.toString(), 'UTF-8')
			println "Writing ASCIIDoc file: ${targetFile.absolutePath}"
		}
	}
}
