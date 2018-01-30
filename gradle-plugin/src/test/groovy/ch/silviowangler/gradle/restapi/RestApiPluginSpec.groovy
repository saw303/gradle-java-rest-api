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

import com.squareup.javapoet.ClassName
import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification

import java.nio.charset.Charset

import static ch.silviowangler.gradle.restapi.Consts.TASK_GROUP_REST_API

class RestApiPluginSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    @Rule
    TemporaryFolder temporaryFolder

    @Shared
    def customFieldModelMapping = { resource, field ->

        if (resource.general.description == 'Natürliche Person') {

            if (field.name == 'leistungsabrechnungspositionen') {
                return ClassName.get(BigDecimal)
            } else if (field.name == 'sprache') {
                return ClassName.get('java.util', 'Locale')
            }
        }
        throw new RuntimeException("Mapping no defined for ${field.name} of resource '${resource.general.description}'")
    }

    void setup() {
        project.apply plugin: 'java'
        project.apply plugin: RestApiPlugin.PLUGIN_ID
    }

    void "The plugin provides the following tasks"() {

        expect:
        project.tasks.findAll { Task task -> task.group == TASK_GROUP_REST_API }.size() == 3

        project.tasks.generateRestArtifacts instanceof GenerateRestApiTask
        project.tasks.generateRestArtifacts.group == TASK_GROUP_REST_API

        and:
        project.tasks.extractSpecs instanceof ExtractRestApiSpecsTask
        project.tasks.extractSpecs.group == TASK_GROUP_REST_API

        and:
        project.tasks.cleanRestArtifacts instanceof CleanRestApiTask
        project.tasks.cleanRestArtifacts.group == TASK_GROUP_REST_API

        and:
        project.extensions.restApi != null
    }


    void "The plugin generates valid Java 8 code for Spring Boot"() {
        given:
        project.restApi.generatorOutput = temporaryFolder.getRoot()
        project.restApi.generatorImplOutput = temporaryFolder.getRoot()
        project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/rootSpringBoot")
        project.restApi.packageName = 'org.acme.rest.v1'
        project.restApi.generateDateAttribute = false
        project.restApi.objectResourceModelMapping = customFieldModelMapping
        project.restApi.springBoot = true

        and:
        GenerateRestApiTask task = project.tasks.generateRestArtifacts

        and:
        String path = project.restApi.packageName.replaceAll('\\.', '/')

        when:
        task.exec()

        and:
        def javaFiles = []
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        new File(temporaryFolder.getRoot(), path).exists()

        and:
        assertGeneratedFiles javaFiles, 3

        and:
        javaFiles.collect {
            it.parent == new File(temporaryFolder.getRoot(), path)
        }.size() == javaFiles.size()

        and: 'Ressourcen validieren'
        assertJavaFile("${project.restApi.packageName}/api/v1", 'RootResource', 'rootSpringBoot')
        assertJavaFile("${project.restApi.packageName}/api/v1", 'RootResourceImpl', 'rootSpringBoot')
        assertJavaFile("${project.restApi.packageName}/api/v1", 'RootGetResourceModel', 'rootSpringBoot')

        when:
        CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts

        and:
        cleanTask.cleanUp()

        and:
        javaFiles.clear()
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        javaFiles.isEmpty()
    }


    void "The plugin generates valid Java 8 code for Spring Boot and the Land/Ort specs"() {

        given:
        project.restApi.generatorOutput = temporaryFolder.getRoot()
        project.restApi.generatorImplOutput = temporaryFolder.getRoot()
        project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/v1")
        project.restApi.packageName = 'org.acme.rest'
        project.restApi.generateDateAttribute = false
        project.restApi.objectResourceModelMapping = customFieldModelMapping
        project.restApi.springBoot = true
        project.restApi.responseEncoding = Charset.forName('UTF-8')

        and:
        GenerateRestApiTask task = project.tasks.generateRestArtifacts

        when:
        task.exec()

        and:
        List<File> javaFiles = []
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        new File(temporaryFolder.getRoot(), 'org/acme/rest').exists()

        and:
        assertGeneratedFiles javaFiles, 14

        and:
        javaFiles.collect {
            it.parent == new File(temporaryFolder.getRoot(), 'org/acme/rest')
        }.size() == javaFiles.size()

        and: 'Ressourcen validieren'
        assertJavaFile('org.acme.rest.v1', 'CoordinatesType', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender', 'LandGetResourceModel', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender', 'LandPostResourceModel', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender', 'LandPutResourceModel', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender', 'LandResource', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender', 'LandResourceImpl', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtGetResourceModel', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPostResourceModel', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPutResourceModel', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResource', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResourceImpl', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1', 'RootResource', 'land-spring-boot')
        assertJavaFile('org.acme.rest.v1', 'RootResourceImpl', 'land-spring-boot')

        when:
        CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts

        and:
        cleanTask.cleanUp()

        and:
        javaFiles.clear()
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        javaFiles.isEmpty()
    }

    void "The plugin generates valid JAX-RS Java 8 code"() {

        given:
        project.restApi.generatorOutput = temporaryFolder.getRoot()
        project.restApi.generatorImplOutput = temporaryFolder.getRoot()
        project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/demo")
        project.restApi.packageName = 'org.acme.rest'
        project.restApi.generateDateAttribute = false
        project.restApi.objectResourceModelMapping = customFieldModelMapping

        and:
        GenerateRestApiTask task = project.tasks.generateRestArtifacts

        when:
        task.exec()

        and:
        def javaFiles = []
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        new File(temporaryFolder.getRoot(), 'org/acme/rest').exists()

        and:
        assertGeneratedFiles javaFiles, 13

        and:
        javaFiles.collect {
            it.parent == new File(temporaryFolder.getRoot(), 'org/acme/rest')
        }.size() == javaFiles.size()

        and: 'Ressourcen validieren'
        assertJavaFile('org.acme.rest.v1.partner', 'PartnerResource')
        assertJavaFile('org.acme.rest.v1.partner', 'PartnerResourceImpl')
        assertJavaFile('org.acme.rest.v1.partner', 'PartnerGetResourceModel')
        assertJavaFile('org.acme.rest.v1.partner', 'PartnerPutResourceModel')
        assertJavaFile('org.acme.rest.v1.partner', 'PartnerPostResourceModel')

        when:
        CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts

        and:
        cleanTask.cleanUp()

        and:
        javaFiles.clear()
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        javaFiles.isEmpty()
    }


    void "Typ Definitionen in der Root Ressource werden berücksichtigt"() {

        given:
        project.restApi.generatorOutput = temporaryFolder.getRoot()
        project.restApi.generatorImplOutput = temporaryFolder.getRoot()
        project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/v1/")
        project.restApi.packageName = 'org.acme.rest'
        project.restApi.generateDateAttribute = false
        project.restApi.objectResourceModelMapping = customFieldModelMapping
        project.restApi.enableSecurity = true
        project.restApi.responseEncoding = Charset.forName('UTF-8')

        and:
        GenerateRestApiTask task = project.tasks.generateRestArtifacts

        when:
        task.exec()

        and:
        def javaFiles = []
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        new File(temporaryFolder.getRoot(), 'org/acme/rest').exists()

        and:
        assertGeneratedFiles javaFiles, 14

        and:
        javaFiles.collect {
            it.parent == new File(temporaryFolder.getRoot(), 'org/acme/rest')
        }.size() == javaFiles.size()

        and: 'Ressourcen validieren'
        assertJavaFile('org.acme.rest.v1', 'CoordinatesType', 'land')
        assertJavaFile('org.acme.rest.v1.laender', 'LandGetResourceModel', 'land')
        assertJavaFile('org.acme.rest.v1.laender', 'LandPutResourceModel', 'land')
        assertJavaFile('org.acme.rest.v1.laender', 'LandPostResourceModel', 'land')
        assertJavaFile('org.acme.rest.v1.laender', 'LandResource', 'land')
        assertJavaFile('org.acme.rest.v1.laender', 'LandResourceImpl', 'land')

        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResource', 'land')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResourceImpl', 'land')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtGetResourceModel', 'land')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPutResourceModel', 'land')
        assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPostResourceModel', 'land')

        assertJavaFile('org.acme.rest.v1', 'RootResource', 'land')
        assertJavaFile('org.acme.rest.v1', 'RootResourceImpl', 'land')
        assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', 'land')

        when:
        CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts

        and:
        cleanTask.cleanUp()

        and:
        javaFiles.clear()
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        javaFiles.isEmpty()
    }

    void "Not specified verbs are explicitly excluded"() {

        given:
        project.restApi.generatorOutput = temporaryFolder.getRoot()
        project.restApi.generatorImplOutput = temporaryFolder.getRoot()
        project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/root/")
        project.restApi.packageName = 'org.acme.rest'
        project.restApi.generateDateAttribute = false
        project.restApi.objectResourceModelMapping = customFieldModelMapping
        project.restApi.responseEncoding = Charset.forName('UTF-8')

        and:
        GenerateRestApiTask task = project.tasks.generateRestArtifacts

        when:
        task.exec()

        and:
        def javaFiles = []
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        new File(temporaryFolder.getRoot(), 'org/acme/rest').exists()

        and:
        assertGeneratedFiles javaFiles, 3

        and:
        javaFiles.collect {
            it.parent == new File(temporaryFolder.getRoot(), 'org/acme/rest')
        }.size() == javaFiles.size()

        and: 'Ressourcen validieren'
        assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', 'root')
        assertJavaFile('org.acme.rest.v1', 'RootResource', 'root')
        assertJavaFile('org.acme.rest.v1', 'RootResourceImpl', 'root')

        when:
        CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts

        and:
        cleanTask.cleanUp()

        and:
        javaFiles.clear()
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        javaFiles.isEmpty()
    }

    @PendingFeature
    void "Das Plugin generiert auch read only Ressourcen mit nur einem Collection GET"() {

        given:
        project.restApi.generatorOutput = temporaryFolder.getRoot()
        project.restApi.generatorImplOutput = temporaryFolder.getRoot()
        project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/collectionOnly")
        project.restApi.packageName = 'org.acme.rest'
        project.restApi.generateDateAttribute = false
        project.restApi.objectResourceModelMapping = customFieldModelMapping

        and:
        GenerateRestApiTask task = project.tasks.generateRestArtifacts

        when:
        task.exec()

        and:
        def javaFiles = []
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        new File(temporaryFolder.getRoot(), 'org/acme/rest').exists()

        and:
        assertGeneratedFiles javaFiles, 3

        and:
        javaFiles.collect {
            it.parent == new File(temporaryFolder.getRoot(), 'org/acme/rest')
        }.size() == javaFiles.size()

        and: 'Ressourcen validieren'
        assertJavaFile('org.acme.rest.v1', 'PartnersearchResource', 'collectionGet')
        assertJavaFile('org.acme.rest.v1', 'PartnersearchResourceImpl', 'collectionGet')
        assertJavaFile('org.acme.rest.v1', 'PartnersearchGetResourceModel', 'collectionGet')

        when:
        CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts

        and:
        cleanTask.cleanUp()

        and:
        javaFiles.clear()
        temporaryFolder.getRoot().eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith('.java')) javaFiles << it
        })

        then:
        javaFiles.isEmpty()
    }

    private void assertJavaFile(String packageName, String className) {
        assertJavaFile(packageName, className, 'default')
    }

    private void assertJavaFile(String packageName, String className, String testName) {
        final String ENCODING = 'UTF-8'
        File expectedJavaFile = new File(temporaryFolder.getRoot().absolutePath + '/' + packageName.replaceAll('\\.', '/'), "${className}.java")
        URL resource = getClass().getResource("/javaOutput/${testName}/${className}.java.txt")
        File actualJavaFile = resource ? new File(resource.file) : new File(className)

        final String expectedJavaSourceCode = expectedJavaFile.exists() ? expectedJavaFile.getText(ENCODING) : "File ${expectedJavaFile.absolutePath} not found"
        final String actualJavaSourceCode = actualJavaFile.exists() ? actualJavaFile.getText(ENCODING) : "File ${actualJavaFile.absolutePath} not found"

        assert expectedJavaSourceCode == actualJavaSourceCode
    }

    private void assertGeneratedFiles(List<File> files, int amount) {
        files.sort { it.name }.each { f -> println "There is file ${f.absolutePath}" }
        assert files.size() == amount
    }
}
