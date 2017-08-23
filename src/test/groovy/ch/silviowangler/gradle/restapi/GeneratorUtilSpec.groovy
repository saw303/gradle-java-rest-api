/*
 * MIT License
 *
 * Copyright (c) 2016 - 2017 Silvio Wangler (silvio.wangler@gmail.com)
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

import com.squareup.javapoet.ClassName
import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.OffsetDateTime
/**
 * Created by Silvio Wangler on 25/01/16.
 */
class GeneratorUtilSpec extends Specification {

    void "Generate Classfile names"() {

        expect:
        GeneratorUtil.createResourceFormDataName(file) == resourceFormData
        GeneratorUtil.createResourceImplementationName(file) == resourceImpl
        GeneratorUtil.createResourceModelName(file) == resourceModel
        GeneratorUtil.createResourceName(file) == resourceClass

        where:

        file                                  || resourceClass       || resourceImpl            || resourceModel               || resourceFormData
        new File('natperson.json')            || 'NatpersonResource' || 'NatpersonResourceImpl' || 'NatpersonGetResourceModel' || 'NatpersonFormData'
        new File('natperson.v1.json')         || 'NatpersonResource' || 'NatpersonResourceImpl' || 'NatpersonGetResourceModel' || 'NatpersonFormData'
        new File('natperson.adresse.v1.json') || 'AdresseResource'   || 'AdresseResourceImpl'   || 'AdresseGetResourceModel'   || 'AdresseFormData'
    }

    void "Generate Classfile names from string"() {

        expect:
        GeneratorUtil.createClassname(classname) == expectedClassname

        where:
        classname        || expectedClassname
        'coordinateType' || 'CoordinateType'
        'silvioWangler'  || 'SilvioWangler'
        'motorway'       || 'Motorway'
    }

    @Unroll
    void "Translate options type '#jsonType' in to Java type"() {

        expect:
        javaType == GeneratorUtil.translateToJava(jsonType)

        where:
        jsonType   || javaType
        'string'   || ClassName.get(String)
        'date'     || ClassName.get(LocalDate)
        'datetime' || ClassName.get(OffsetDateTime)
        'decimal'  || ClassName.get(BigDecimal)
        'int'      || ClassName.get(Integer)
        'double'   || ClassName.get(Double)
        'float'    || ClassName.get(Double)
        'bool'     || ClassName.get(Boolean)
        'flag'     || ClassName.get(Boolean)
        'object'   || ClassName.get(Object)
        'money'    || ClassName.get('org.javamoney.moneta', 'Money')
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
        'POST'              || 'Post'
        'PUT'               || 'Put'
        'PATCH'             || 'Patch'
        'DELETE_ENTITY'     || 'Delete'
        'DELETE_COLLECTION' || 'Delete'
    }

    @Unroll
    void "#a plus #b is equals #c"() {
        expect:
        a + b == c

        where:
        a | b || c
        1 | 1 || 2
        1 | 2 || 3
    }

    @Unroll
    void "Expect packagename #packagename"() {

        given:
        def slurper = new JsonSlurper(type: JsonParserType.INDEX_OVERLAY)

        expect:
        GeneratorUtil.composePackageName(slurper.parse(json.getBytes())) == packagename

        where:
        json                                                                                                || packagename
        '{"general": {"version": "1.0.0","x-route": "/:version/laender/:entity"}}'                          || 'v1'
        '{"general": {"version": "1.0.0","x-route": "/v1/laender/:entity"}}'                                || 'v1'
        '{"general": {"version": "1.1.0","x-route": "/:version/laender/:entity"}}'                          || 'v1'
        '{"general": {"version": "1.1.1","x-route": "/:version/laender/:entity"}}'                          || 'v1'
        '{"general": {"version": "1.0.0","x-route": "/:version/laender/:land/orte/:entity"}}'               || 'v1.laender'
        '{"general": {"version": "1.0.0","x-route": "/:version/laender/:land/orte/:ort/strassen/:entity"}}' || 'v1.laender.orte'
        '{"general": {"version": "2.0.0","x-route": "/:version/laender/:land/orte/:entity"}}'               || 'v2.laender'
    }
}
