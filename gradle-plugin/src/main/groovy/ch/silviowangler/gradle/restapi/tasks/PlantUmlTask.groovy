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

import ch.silviowangler.gradle.restapi.builder.ResourceContractContainer
import ch.silviowangler.gradle.restapi.diagrams.Dependency
import ch.silviowangler.gradle.restapi.diagrams.Knot
import ch.silviowangler.rest.contract.model.v1.SubResource
import groovy.text.SimpleTemplateEngine
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.GeneratedImage
import net.sourceforge.plantuml.ISourceFileReader
import net.sourceforge.plantuml.SourceFileReader
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import static net.sourceforge.plantuml.FileFormat.SVG

/**
 * @author Silvio Wangler
 */
class PlantUmlTask extends SpecificationBaseTask {

	private SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

	@OutputDirectory
	File getRootOutputDir() {
		project.restApi.diagramOutput
	}

	@TaskAction
	void generateDiagrams() {

		List<File> specs = findSpecifications(getOptionsSource())
		List<ResourceContractContainer> contracts = []
		specs.each { File specFile -> contracts << specGenerator.parseResourceContract(specFile) }

		ResourceContractContainer root = contracts.find { ResourceContractContainer c -> c.resourceContract.general.name == 'root' }

		Knot<ResourceContractContainer> hierarchy = buildHierarchy(root, contracts)

		String resource = '/puml/resources-overview.puml.template'
		URL url = getClass().getResource(resource)
		def template = templateEngine.createTemplate(url).make([title: 'Resources Overview', containers: contracts, dependencies: buildDependencyList(hierarchy), showFields: project.restApi.diagramShowFields])

		File targetFile = new File(getRootOutputDir(), 'resources-overview.puml')

		if (targetFile.exists()) {
			targetFile.delete()
		}
		targetFile.createNewFile()

		targetFile.write(template.toString(), 'UTF-8')

		ISourceFileReader reader = new SourceFileReader(targetFile)

		for (GeneratedImage image in reader.getGeneratedImages()) {
			logger.info("Wrote PNG file {}", image.getPngFile().absolutePath)
		}

		reader.setFileFormatOption(new FileFormatOption(SVG))

		for (GeneratedImage image in reader.getGeneratedImages()) {
			logger.info("Wrote SVG file {}", image.getPngFile().absolutePath)
		}
	}


	private Knot<ResourceContractContainer> buildHierarchy(ResourceContractContainer container, List<ResourceContractContainer> containers) {
		return buildHierarchy(container, null, containers)
	}

	private Knot<ResourceContractContainer> buildHierarchy(ResourceContractContainer container, ResourceContractContainer parent, List<ResourceContractContainer> containers) {

		Knot<ResourceContractContainer> node = new Knot(container, parent)

		for (SubResource subResource in container.resourceContract.subresources) {

			ResourceContractContainer subNode = findSubResourceContract(containers, subResource)

			if (subNode) {
				node.children << buildHierarchy(subNode, containers)
			} else {
				println "Cannot find contract for sub resource ${subResource.name}"
			}
		}

		return node
	}

	/**
	 * This method first searches for subresources by name and disambiguites conflicts using an xRoute longest common prefix match.
	 * If two resources have the same name, the resource with an xRoute with the most in common with the subresource.href will be choosen.
	 * @param containers a list of all ResourceContractContainers
	 * @param subResource a Subresource spec
	 * @return the ResourceContractContainer for the resource pointed to by `subResource`
	 */
	static ResourceContractContainer findSubResourceContract(List<ResourceContractContainer> containers, SubResource subResource) {

		List<ResourceContractContainer> results = containers.findAll { ResourceContractContainer c -> c.resourceContract.general.name == subResource.name }
		if (results.size() > 1) {
			// remove `{` and `}` from the href
			String subresourceRoute = subResource.href.replace("{", "").replace("}", "")

			// pattern for `:pin` variable names, since they don't have to be the same between xRoute and href
			// we remove them from the string
			String pattern = ':[^/]+'
			subresourceRoute = subresourceRoute.replaceAll(pattern, "")

			// distinguish by xRoute matching the longest common prefix we can find
			ResourceContractContainer match
			int matchLen = -1
			for (ResourceContractContainer c : results) {
				String resourceRoute = c.resourceContract.general.xRoute.replaceAll(pattern, "")
				String commonPrefix = commonPrefix(resourceRoute, subresourceRoute)
				if (commonPrefix.length() > matchLen) {
					match = c
					matchLen = commonPrefix.length()
				}
			}
			return match

		} else if (results.size() == 1) {
			return results[0]
		} else {
			return null
		}

	}

	Set<Dependency> buildDependencyList(Knot<ResourceContractContainer> tree) {
		return buildDependencyList(tree, [] as Set)
	}

	Set<Dependency> buildDependencyList(Knot<ResourceContractContainer> tree, Set<Dependency> dependencies) {

		if (!tree.children.isEmpty()) {

			for (child in tree.children) {
				dependencies << new Dependency(parent: tree.data.resourceContract.general.hashCode().abs(), child: child.data.resourceContract.general.hashCode().abs())
				dependencies.addAll(buildDependencyList(child, dependencies))
			}
		}
		return dependencies
	}

	/**
	 * @param a a String
	 * @param b another String
	 * @return the longest common substring prefix of a and b. If a or b are null, return the empty string
	 */
	static String commonPrefix(String a, String b) {
		if (a == null || a.isEmpty()) {
			return ""
		}
		if (b == null || b.isEmpty()) {
			return ""
		}
		int len = Math.min(a.size(), b.size())
		int counter = 0
		for (; counter < len; counter++) {
			if (a.charAt(counter) != b.charAt(counter)) {
				break
			}
		}
		return a.substring(0, counter)
	}
}
