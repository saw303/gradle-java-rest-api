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


import ch.silviowangler.rest.contract.model.v1.Representation
import com.google.common.base.CaseFormat
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import org.gradle.api.Project

import static ch.silviowangler.gradle.restapi.TargetFramework.JAX_RS
import static ch.silviowangler.gradle.restapi.TargetFramework.MICRONAUT
import static ch.silviowangler.gradle.restapi.TargetFramework.MICRONAUT_24

/**
 * Created by Silvio Wangler on 25/01/16.
 */
class GeneratorUtil {

	private static String createTypeName(String fileName, String type) {
		return createTypeName(fileName, type, '')
	}

	private static String createTypeName(String fileName, String type, String verb) {

		String postfix

		if (type == Consts.RESOURCE_MODEL) postfix = 'ResourceModel'
		else if (type == Consts.RESOURCE_FORM_DATA) postfix = 'FormData'
		else if (type == Consts.RESOURCE) postfix = 'Resource'
		else if (type == Consts.RESOURCE_IMPLEMENTATION) postfix = 'ResourceImpl'
		else if (type == Consts.RESOURCE_DELEGATION) postfix = 'ResourceDelegate'
		else throw new IllegalArgumentException("Unknown param value ${type}")


		def filenameWithoutExtension = fileName.replace('.json', '').replaceAll('\\.v\\d', '')

		if (filenameWithoutExtension.contains('.')) {
			String[] split = filenameWithoutExtension.split('\\.')
			filenameWithoutExtension = split[split.length - 1]
		}

		return "${filenameWithoutExtension[0].toUpperCase()}${filenameWithoutExtension[1..filenameWithoutExtension.length() - 1]}${verb}${postfix}"
	}

	static String createResourceModelName(String fileName, final String verb = "Get") {
		createTypeName(fileName, Consts.RESOURCE_MODEL, verb)
	}

	static String createResourceName(String fileName) {
		createTypeName(fileName, Consts.RESOURCE)
	}

	static String createResourceFormDataName(String fileName) {
		createTypeName(fileName, Consts.RESOURCE_FORM_DATA)
	}

	static String createResourceImplementationName(String fileName) {
		createTypeName(fileName, Consts.RESOURCE_IMPLEMENTATION)
	}

	static String createResourceDelegateName(String fileName) {
		createTypeName(fileName, Consts.RESOURCE_DELEGATION)
	}

	static String verb(final String verb) {
		String v = verb
		if (verb.contains('_')) {
			v = verb.split('_')[0]
		}
		return "${v[0].toUpperCase()}${v[1..v.length() - 1].toLowerCase()}"
	}

	static TypeName getMicronautReturnType(String fileName, String verb, boolean collection = false, String packageName, Representation representation) {

		if (representation.name != "json" || representation.isRaw()) {
			return PluginTypes.MICRONAUT_HTTP_RESPONSE.className
		}

		return getReturnType(fileName, verb, collection, packageName, MICRONAUT)
	}

	static TypeName getSpringBootReturnType(String fileName, String verb, boolean collection = false, String packageName, Representation representation) {

		if (representation.name != "json") {
			return PluginTypes.SPRING_RESPONSE_ENTITY.className
		}

		return getReturnType(fileName, verb, collection, packageName, TargetFramework.SPRING_BOOT)
	}

	static TypeName getJaxRsReturnType(String fileName,
			String verb,
			boolean collection = false,
			String packageName,
			Representation representation) {

		if (representation.name != "json") {
			return PluginTypes.JAX_RS_RESPONSE.className
		}
		return getReturnType(fileName, verb, collection, packageName, JAX_RS)
	}

	private static TypeName getReturnType(String fileName,
			String verb,
			boolean collection = false,
			String packageName,
			TargetFramework targetFramework = JAX_RS) {

		if (verb == 'Get') {
			String resourceModelName = createResourceModelName(fileName, verb)
			if (collection) {
				return ParameterizedTypeName.get(ClassName.get(targetFramework.micronaut ? Iterable.class : Collection.class), ClassName.get(packageName, resourceModelName))
			} else {
				return ClassName.get(packageName, resourceModelName)
			}
		} else if (verb == 'Head') {
			switch (targetFramework) {
				case JAX_RS:
					return PluginTypes.JAX_RS_RESPONSE.typeName
				case TargetFramework.SPRING_BOOT:
					return PluginTypes.SPRING_RESPONSE_ENTITY.typeName
				case MICRONAUT:
				case MICRONAUT_24:
					return PluginTypes.MICRONAUT_HTTP_RESPONSE.typeName
				default:
					throw new RuntimeException("Unknown framework ${targetFramework}")
			}
		} else if (verb == 'Put' || verb == 'Post') {
			if (collection) {
				switch (targetFramework) {
					case JAX_RS:
						return PluginTypes.JAX_RS_RESPONSE.typeName
					case TargetFramework.SPRING_BOOT:
						return PluginTypes.SPRING_RESPONSE_ENTITY.typeName
					case MICRONAUT:
					case MICRONAUT_24:
						return PluginTypes.MICRONAUT_HTTP_RESPONSE.typeName
					default:
						throw new RuntimeException("Unknown framework ${targetFramework}")
				}
			} else {
				return PluginTypes.RESTAPI_IDTYPE.typeName
			}
		} else if (verb == 'Delete') {

			switch (targetFramework) {
				case JAX_RS:
					return PluginTypes.JAX_RS_RESPONSE.typeName
				case TargetFramework.SPRING_BOOT:
					return PluginTypes.SPRING_RESPONSE_ENTITY.typeName
				case MICRONAUT:
				case MICRONAUT_24:
					return TypeName.VOID
				default:
					throw new RuntimeException("Unknown framework ${targetFramework}")
			}
		} else {
			throw new RuntimeException("Unknown verb ${verb}")
		}
	}

	static String createClassName(String value) {
		return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, value)
	}

	static File generatorInput(Project project) {
		return new File(project.buildDir, 'rest-api-specs')
	}
}
