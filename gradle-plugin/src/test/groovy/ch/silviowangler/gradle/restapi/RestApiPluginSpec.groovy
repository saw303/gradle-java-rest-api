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
package ch.silviowangler.gradle.restapi


import ch.silviowangler.gradle.restapi.tasks.CleanRestApiTask
import ch.silviowangler.gradle.restapi.tasks.ExtractRestApiSpecsTask
import ch.silviowangler.gradle.restapi.tasks.GenerateRestApiTask
import ch.silviowangler.gradle.restapi.tasks.PlantUmlTask
import ch.silviowangler.gradle.restapi.tasks.ValidationTask
import com.squareup.javapoet.ClassName
import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.charset.Charset

import static ch.silviowangler.gradle.restapi.Consts.TASK_GROUP_REST_API
import static ch.silviowangler.gradle.restapi.TargetFramework.MICRONAUT
import static ch.silviowangler.gradle.restapi.TargetFramework.MICRONAUT_24
import static ch.silviowangler.gradle.restapi.TargetFramework.MICRONAUT_3
import static ch.silviowangler.gradle.restapi.TargetFramework.SPRING_BOOT

class RestApiPluginSpec extends Specification {

  Project project = ProjectBuilder.builder().build()

  @TempDir
  File tempDir

  @Shared
  def customFieldModelMapping = { resource, field ->

    if (resource.general.description == 'Natürliche Person') {

      if (field.name == 'leistungsabrechnungspositionen') {
        return ClassName.get(BigDecimal)
      } else if (field.name == 'sprache') {
        return ClassName.get(Locale)
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
    project.tasks.findAll { task -> task.group == TASK_GROUP_REST_API }.size() == 6

    and:
    project.tasks.validateRestSpecs instanceof ValidationTask
    project.tasks.validateRestSpecs.group == TASK_GROUP_REST_API

    and:
    project.tasks.generateRestArtifacts instanceof GenerateRestApiTask
    project.tasks.generateRestArtifacts.dependsOn*.type == [ValidationTask]
    project.tasks.generateRestArtifacts.group == TASK_GROUP_REST_API

    and:
    project.tasks.extractSpecs instanceof ExtractRestApiSpecsTask
    project.tasks.extractSpecs.group == TASK_GROUP_REST_API

    and:
    project.tasks.cleanRestArtifacts instanceof CleanRestApiTask
    project.tasks.cleanRestArtifacts.group == TASK_GROUP_REST_API

    and:
    project.tasks.generateDiagrams instanceof PlantUmlTask
    project.tasks.generateDiagrams.group == TASK_GROUP_REST_API

    and:
    project.extensions.restApi != null
  }


  void "The plugin generates valid Java 8 code for Spring Boot"() {
    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/rootSpringBoot")
    project.restApi.packageName = 'org.acme.rest.v1'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.targetFramework = SPRING_BOOT

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    and:
    String path = project.restApi.packageName.replaceAll('\\.', '/')

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, path).exists()

    and:
    assertGeneratedFiles javaFiles, 6

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, path)
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile("${project.restApi.packageName}/api/v1", 'RootResource', 'rootSpringBoot')
    assertJavaFile("${project.restApi.packageName}/api/v1", 'RootResourceImpl', 'rootSpringBoot')
    assertJavaFile("${project.restApi.packageName}/api/v1", 'RootGetResourceModel', 'rootSpringBoot')
    assertJavaFile("${project.restApi.packageName}/api/v1", 'RootPutResourceModel', 'rootSpringBoot')
    assertJavaFile("${project.restApi.packageName}/api/v1", 'NeedType', 'rootSpringBoot')
    assertJavaFile("${project.restApi.packageName}/api/v1", 'InsuranceNeedsGroupType', 'rootSpringBoot')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()

    when:
    ValidationTask validationTask = project.tasks.validateRestSpecs as ValidationTask

    and:
    validationTask.validate()

    then:
    noExceptionThrown()
  }


  void "The plugin generates valid Java 8 code for Spring Boot and the Land-Ort specs"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/v1")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.targetFramework = SPRING_BOOT
    project.restApi.responseEncoding = Charset.forName('UTF-8')

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    List<File> javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 15

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1', 'CoordinatesType', 'land')
    assertJavaFile('org.acme.rest.v1', 'DummyType', 'land')
    assertJavaFile('org.acme.rest.v1.laender', 'LandGetResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender', 'LandPostResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender', 'LandPutResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender', 'LandResource', 'land-spring-boot')
    assertJavaFile('org.acme.rest.v1.laender', 'LandResourceImpl', 'land-spring-boot')
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtGetResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPostResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPutResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResource', 'land-spring-boot')
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResourceImpl', 'land-spring-boot')
    assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1', 'RootResource', 'land-spring-boot')
    assertJavaFile('org.acme.rest.v1', 'RootResourceImpl', 'land-spring-boot')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "The plugin generates valid Java 8 code for Micronaut and the Land-Ort specs"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/v1")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.targetFramework = MICRONAUT
    project.restApi.responseEncoding = Charset.forName('UTF-8')

    String testName = 'land-micronaut'

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    List<File> javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 15

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1', 'CoordinatesType', 'land')
    assertJavaFile('org.acme.rest.v1', 'DummyType', 'land')
    assertJavaFile('org.acme.rest.v1.laender', 'LandGetResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender', 'LandPostResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandPutResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandResource', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandResourceDelegate', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtGetResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPostResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPutResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResource', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResourceDelegate', testName)
    assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', 'land')
    assertJavaFile('org.acme.rest.v1', 'RootResource', testName)
    assertJavaFile('org.acme.rest.v1', 'RootResourceDelegate', testName)

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "The plugin generates valid JAX-RS Java 8 code"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/demo")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 14

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1.partner', 'PartnerResource')
    assertJavaFile('org.acme.rest.v1.partner', 'PartnerResourceImpl')
    assertJavaFile('org.acme.rest.v1.partner', 'PartnerGetResourceModel')
    assertJavaFile('org.acme.rest.v1.partner', 'PartnerPostResourceModel')
    assertJavaFile('org.acme.rest.v1.partner', 'PartnerPutResourceModel')
    assertJavaFile('org.acme.rest.v1.partner', 'GenderType')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }


  void "Typ definitions of the root resource are respected"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/v1/")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.enableSecurity = true
    project.restApi.responseEncoding = Charset.forName('UTF-8')

    and:
    final String testName = 'land'

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 15

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1', 'CoordinatesType', testName)
    assertJavaFile('org.acme.rest.v1', 'DummyType', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandGetResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandPostResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandPutResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandResource', testName)
    assertJavaFile('org.acme.rest.v1.laender', 'LandResourceImpl', testName)

    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtGetResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPostResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtPutResourceModel', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResource', testName)
    assertJavaFile('org.acme.rest.v1.laender.orte', 'OrtResourceImpl', testName)

    assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', testName)
    assertJavaFile('org.acme.rest.v1', 'RootResource', testName)
    assertJavaFile('org.acme.rest.v1', 'RootResourceImpl', testName)

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "Empty verbs and fields in root works"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/rootSimple/")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.responseEncoding = Charset.forName('UTF-8')
    project.restApi.targetFramework = SPRING_BOOT

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 2

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1', 'RootResource', 'rootSimple')
    assertJavaFile('org.acme.rest.v1', 'RootResourceImpl', 'rootSimple')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "Not specified verbs are explicitly excluded"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/root/")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.responseEncoding = Charset.forName('UTF-8')

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 3

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', 'root')
    assertJavaFile('org.acme.rest.v1', 'RootResource', 'root')
    assertJavaFile('org.acme.rest.v1', 'RootResourceImpl', 'root')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "The plugin refuses to generate a resource containing a HEAD verb without a corresponding GET verb"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/invalid")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.targetFramework = MICRONAUT
    project.restApi.objectResourceModelMapping = customFieldModelMapping

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    then:
    IllegalStateException ex = thrown(IllegalStateException)

    and:
    ex.message == "Verb: [HEAD_ENTITY] Representation: [json] has no GET counterpart"
  }

  void "The plugin generates read only resources with query parameter and explicit extension (Micronaut)"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/search")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.targetFramework = MICRONAUT
    project.restApi.objectResourceModelMapping = customFieldModelMapping

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 3

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1.search', 'SearchResource', 'search-micronaut')
    assertJavaFile('org.acme.rest.v1.search', 'SearchResourceDelegate', 'search-micronaut')
    assertJavaFile('org.acme.rest.v1.search', 'SearchGetResourceModel', 'search-micronaut')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "The plugin generates read only resources with query parameter and explicit extension (Micronaut Client)"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/search")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.targetFramework = MICRONAUT
    project.restApi.generationMode = GenerationMode.CLIENT
    project.restApi.objectResourceModelMapping = customFieldModelMapping

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 2

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1.search', 'SearchGetResourceModel', 'search-micronaut')
    assertJavaFile('org.acme.rest.v1.search', 'SearchResourceClient', 'search-micronaut-client')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "Generate Micronaut Client for Membership Fees"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/membershipfees")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.targetFramework = MICRONAUT_3
    project.restApi.generationMode = GenerationMode.CLIENT

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 3

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.api.v1.admin.seasons.membershipfees', 'MembershipfeesPostResourceModel', 'membershipfees')
    assertJavaFile('org.acme.rest.api.v1.admin.seasons.membershipfees', 'MembershipfeesGetResourceModel', 'membershipfees')
    assertJavaFile('org.acme.rest.api.v1.admin.seasons.membershipfees', 'MembershipfeesResourceClient', 'membershipfees')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "Das Plugin generiert auch read only Ressourcen mit nur einem CSV GET (Micronaut)"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/csvOnly")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.targetFramework = MICRONAUT
    project.restApi.objectResourceModelMapping = customFieldModelMapping

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 2

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1.download', 'DownloadResource', 'csvOnly')
    assertJavaFile('org.acme.rest.v1.download', 'DownloadResourceDelegate', 'csvOnly')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "Read only resource with only JSON Collection GET"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/collectionOnly")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 3

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1.partner', 'PartnersearchResource', 'collectionOnly')
    assertJavaFile('org.acme.rest.v1.partner', 'PartnersearchResourceImpl', 'collectionOnly')
    assertJavaFile('org.acme.rest.v1.partner', 'PartnersearchGetResourceModel', 'collectionOnly')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "Das Plugin generiert auch read only Ressourcen mit nur einem Collection GET (Spring Boot)"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/proposal")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.targetFramework = SPRING_BOOT

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    def javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 6

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1.session.insurableperson.insuranceneeds.productproposal', 'ProductproposalResource', 'proposal-spring-boot')
    assertJavaFile('org.acme.rest.v1.session.insurableperson.insuranceneeds.productproposal', 'ProductproposalResourceImpl', 'proposal-spring-boot')
    assertJavaFile('org.acme.rest.v1.session.insurableperson.insuranceneeds.productproposal', 'ProductproposalGetResourceModel', 'proposal-spring-boot')
    assertJavaFile('org.acme.rest.v1.session.insurableperson.insuranceneeds.productproposal', 'ProductproposalPostResourceModel', 'proposal-spring-boot')
    assertJavaFile('org.acme.rest.v1.session.insurableperson.insuranceneeds.productproposal', 'ProductproposalPutResourceModel', 'proposal-spring-boot')
    assertJavaFile('org.acme.rest.v1.session.insurableperson.insuranceneeds.productproposal', 'ProposalType', 'proposal-spring-boot')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  void "The plugin can generate resource diagrams"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/rootSpringBoot")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.targetFramework = SPRING_BOOT
    project.restApi.diagramOutput = tempDir

    and:
    PlantUmlTask task = project.tasks.generateDiagrams as PlantUmlTask

    when:
    task.generateDiagrams()

    and:
    def files = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.puml')) files << it
    })

    then:
    files.size() == 1

    and:
    assertPlantUmlFile('resources-overview.puml', 'resources-overview.puml', 'rootSpringBoot')
  }

  @Unroll
  void "The plugin can generate resource diagrams for land/ort showfields: #showFields"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/v1")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.targetFramework = SPRING_BOOT
    project.restApi.diagramOutput = tempDir
    project.restApi.diagramShowFields = showFields

    and:
    PlantUmlTask task = project.tasks.generateDiagrams as PlantUmlTask

    when:
    task.generateDiagrams()

    and:
    def files = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.puml')) files << it
    })

    then:
    files.size() == 1

    and:
    assertPlantUmlFile('resources-overview.puml', resourceName, 'land')
    where:
    showFields || resourceName
    false      || 'resources-overview.puml'
    true       || 'resources-overview-fields.puml'
  }

  void "No id field is present in a resource"() {

    given:
    project.restApi.generatorOutput = tempDir
    project.restApi.generatorImplOutput = tempDir
    project.restApi.optionsSource = new File("${new File('').absolutePath}/src/test/resources/specs/root-no-id")
    project.restApi.packageName = 'org.acme.rest'
    project.restApi.generateDateAttribute = false
    project.restApi.objectResourceModelMapping = customFieldModelMapping
    project.restApi.targetFramework = MICRONAUT_3
    project.restApi.responseEncoding = Charset.forName('UTF-8')

    and:
    GenerateRestApiTask task = project.tasks.generateRestArtifacts as GenerateRestApiTask

    when:
    task.exec()

    and:
    List<File> javaFiles = []
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    new File(tempDir, 'org/acme/rest').exists()

    and:
    assertGeneratedFiles javaFiles, 3

    and:
    javaFiles.collect {
      it.parent == new File(tempDir, 'org/acme/rest')
    }.size() == javaFiles.size()

    and: 'validate resources'
    assertJavaFile('org.acme.rest.v1', 'RootGetResourceModel', 'root-no-id')
    assertJavaFile('org.acme.rest.v1', 'RootResource', 'root-no-id')
    assertJavaFile('org.acme.rest.v1', 'RootResourceDelegate', 'root-no-id')

    when:
    CleanRestApiTask cleanTask = project.tasks.cleanRestArtifacts as CleanRestApiTask

    and:
    cleanTask.cleanUp()

    and:
    javaFiles.clear()
    tempDir.eachFileRecurse(FileType.FILES, {
      if (it.name.endsWith('.java')) javaFiles << it
    })

    then:
    javaFiles.isEmpty()
  }

  private void assertPlantUmlFile(String expectedFileName, String actualFileName, String testSetName) {
    final String ENCODING = 'UTF-8'
    File expectedFile = new File(tempDir, expectedFileName)
    URL resource = getClass().getResource("/puml/${testSetName}/${actualFileName}")
    File actualFile = new File(resource.file)

    final String expectedSourceCode = expectedFile.exists() ? expectedFile.getText(ENCODING) : "File ${expectedFile.absolutePath} not found"
    final String actualSourceCode = actualFile.exists() ? actualFile.getText(ENCODING) : "File ${actualFile.absolutePath} not found"

    assert expectedSourceCode == actualSourceCode
  }

  private void assertJavaFile(String packageName, String className) {
    assertJavaFile(packageName, className, 'default')
  }

  private void assertJavaFile(String packageName, String className, String testName) {
    final String ENCODING = 'UTF-8'
    File expectedJavaFile = new File(tempDir.absolutePath + '/' + packageName.replaceAll('\\.', '/'), "${className}.java")
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
