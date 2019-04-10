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

import ch.silviowangler.gradle.restapi.builder.ResourceBuilder
import ch.silviowangler.rest.contract.model.v1.FieldType
import ch.silviowangler.rest.contract.model.v1.ResourceField
import com.squareup.javapoet.ClassName
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant
import java.time.LocalDate

/**
 * Created by Silvio Wangler on 25/01/16.
 */
class GeneratorUtilSpec extends Specification {

  void "Generate Classfile names"() {

    expect:
    GeneratorUtil.createResourceFormDataName(file.name) == resourceFormData
    GeneratorUtil.createResourceImplementationName(file.name) == resourceImpl
    GeneratorUtil.createResourceModelName(file.name) == resourceModel
    GeneratorUtil.createResourceName(file.name) == resourceClass
    GeneratorUtil.createResourceDelegateName(file.name) == resourceDelegate

    where:

    file                                  || resourceClass       || resourceImpl            || resourceModel               || resourceFormData    || resourceDelegate
    new File('natperson.json')            || 'NatpersonResource' || 'NatpersonResourceImpl' || 'NatpersonGetResourceModel' || 'NatpersonFormData' || 'NatpersonResourceDelegate'
    new File('natperson.v1.json')         || 'NatpersonResource' || 'NatpersonResourceImpl' || 'NatpersonGetResourceModel' || 'NatpersonFormData' || 'NatpersonResourceDelegate'
    new File('natperson.adresse.v1.json') || 'AdresseResource'   || 'AdresseResourceImpl'   || 'AdresseGetResourceModel'   || 'AdresseFormData'   || 'AdresseResourceDelegate'
  }

  void "Generate Classfile names from string"() {

    expect:
    GeneratorUtil.createClassName(classname) == expectedClassname

    where:
    classname        || expectedClassname
    'coordinateType' || 'CoordinateType'
    'silvioWangler'  || 'SilvioWangler'
    'motorway'       || 'Motorway'
  }

  @Unroll
  void "Translate options type '#jsonType' in to Java type"() {

    given:
    FieldType fieldType = new ResourceField(type: jsonType)

    when:
    ClassName className = ResourceBuilder.JavaTypeRegistry.translateToJava(fieldType)

    then:
    className == javaType

    where:
    jsonType   || javaType
    'string'   || ClassName.get(String)
    'email'    || ClassName.get(String)
    'date'     || ClassName.get(LocalDate)
    'datetime' || ClassName.get(Instant)
    'decimal'  || ClassName.get(BigDecimal)
    'int'      || ClassName.get(Integer)
    'double'   || ClassName.get(Double)
    'float'    || ClassName.get(Double)
    'bool'     || ClassName.get(Boolean)
    'flag'     || ClassName.get(Boolean)
    'object'   || ClassName.get(Object)
    'money'    || ClassName.get('javax.money', 'MonetaryAmount')
  }

  @Unroll
  void "Aus #verb wird #expectedValue"() {
    expect:
    expectedValue == GeneratorUtil.verb(verb)

    where:
    verb                || expectedValue
    'GET_COLLECTION'    || 'Get'
    'GET_ENTITY'        || 'Get'
    'GET'               || 'Get'
    'HEAD_COLLECTION'   || 'Head'
    'HEAD_ENTITY'       || 'Head'
    'HEAD'              || 'Head'
    'POST'              || 'Post'
    'PUT'               || 'Put'
    'PATCH'             || 'Patch'
    'DELETE_ENTITY'     || 'Delete'
    'DELETE_COLLECTION' || 'Delete'
  }
}
