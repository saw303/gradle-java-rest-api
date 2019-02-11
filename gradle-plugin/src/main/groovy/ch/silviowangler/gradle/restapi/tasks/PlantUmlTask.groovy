/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2019 Silvio Wangler (silvio.wangler@gmail.com)
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
import ch.silviowangler.gradle.restapi.Specification
import ch.silviowangler.gradle.restapi.builder.ResourceContractContainer
import ch.silviowangler.gradle.restapi.builder.SpecGenerator
import ch.silviowangler.gradle.restapi.diagrams.Dependency
import ch.silviowangler.gradle.restapi.diagrams.Knot
import ch.silviowangler.rest.contract.model.v1.SubResource
import groovy.text.SimpleTemplateEngine
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * @author Silvio Wangler
 */
class PlantUmlTask extends AbstractTask implements Specification {

    private SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

    @InputDirectory
    File getOptionsSource() {
        if (project.restApi.optionsSource) {
            return project.restApi.optionsSource
        }
        return new File(GeneratorUtil.generatorInput(project), "spec")
    }

    @OutputDirectory
    File getRootOutputDir() {
        project.restApi.diagramOutput
    }

    @TaskAction
    void generateDiagrams() {

        List<File> specs = findSpecifications(getOptionsSource())
        List<ResourceContractContainer> contracts = []
        specs.each { File specFile -> contracts << SpecGenerator.parseResourceContract(specFile) }

        ResourceContractContainer root = contracts.find { ResourceContractContainer c -> c.resourceContract.general.name == 'root' }

        Knot<ResourceContractContainer> hierarchy = buildHierarchy(root, contracts)

        URL url = getClass().getResource('/puml/resources-overview.puml.template')
        def template = templateEngine.createTemplate(url).make([title: 'Resources Overview', containers: contracts, dependencies: buildDependencyList(hierarchy)])

        File targetFile = new File(getRootOutputDir(), 'resources-overview.puml')

        if (targetFile.exists()) {
            targetFile.delete()
        }
        targetFile.createNewFile()

        targetFile.write(template.toString(), 'UTF-8')
    }

    private Knot<ResourceContractContainer> buildHierarchy(ResourceContractContainer container, List<ResourceContractContainer> containers) {
        return buildHierarchy(container, null, containers)
    }

    private Knot<ResourceContractContainer> buildHierarchy(ResourceContractContainer container, ResourceContractContainer parent, List<ResourceContractContainer> containers) {

        Knot<ResourceContractContainer> node = new Knot(container, parent)

        for (SubResource subResource in container.resourceContract.subresources) {

            ResourceContractContainer subNode = containers.find { ResourceContractContainer c -> c.resourceContract.general.name == subResource.name }

            if (subNode) {
                node.children << buildHierarchy(subNode, containers)
            } else {
                println "Cannot find contract for sub resource ${subResource.name}"
            }
        }

        return node
    }

    Set<Dependency> buildDependencyList(Knot<ResourceContractContainer> tree) {
        return buildDependencyList(tree, [] as Set)
    }

    Set<Dependency> buildDependencyList(Knot<ResourceContractContainer> tree, Set<Dependency> dependencies) {

        if (!tree.children.isEmpty()) {

            for (child in tree.children) {
                dependencies << new Dependency(parent: tree.data.resourceContract.general.name, child: child.data.resourceContract.general.name)
                dependencies.addAll(buildDependencyList(child, dependencies))
            }
        }
        return dependencies
    }
}
