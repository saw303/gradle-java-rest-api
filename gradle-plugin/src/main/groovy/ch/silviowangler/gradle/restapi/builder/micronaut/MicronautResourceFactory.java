/**
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
package ch.silviowangler.gradle.restapi.builder.micronaut;

import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.PluginTypes;
import ch.silviowangler.gradle.restapi.builder.AbstractResourceBuilder;
import ch.silviowangler.gradle.restapi.builder.ArtifactType;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.Verb;
import ch.silviowangler.rest.contract.model.v1.VerbParameter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_INJECT;
import static ch.silviowangler.gradle.restapi.PluginTypes.JAVAX_SINGLETON;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_CONTROLLER;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_DELETE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_GET;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_HTTP_RESPONSE;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_OPTIONS;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_POST;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_PRODUCES;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_PUT;
import static ch.silviowangler.gradle.restapi.PluginTypes.MICRONAUT_QUERY_VALUE;
import static ch.silviowangler.gradle.restapi.builder.ArtifactType.DELEGATOR_RESOURCE;

/**
 * @author Silvio Wangler
 */
public class MicronautResourceFactory extends AbstractResourceBuilder {

	private static final ClassName STRING_CLASS = ClassName.get(String.class);
	private static final String DELEGATE_VAR_NAME = "delegate";
	private final boolean explicitExtensions;

	public MicronautResourceFactory(boolean explicitExtensions) {
		this.explicitExtensions = explicitExtensions;
	}

	@Override
	protected void createOptionsMethod() {
		Verb verb = new Verb();
		verb.setVerb("OPTIONS");

		setCurrentVerb(verb);
		MethodSpec.Builder optionsMethod = createMethod("getOptions", STRING_CLASS);

		optionsMethod.addStatement("return OPTIONS_CONTENT");

		resourceBaseTypeBuilder().addMethod(optionsMethod.build());
		setCurrentVerb(null);
	}

	@Override
	public TypeSpec buildResource() {
		reset();
		setArtifactType(DELEGATOR_RESOURCE);
		TypeSpec.Builder resourceBuilder = resourceBaseTypeBuilder();

		Map<String, Object> args = new HashMap<>();
		args.put("value", getPath());

		resourceBuilder.addAnnotation(createAnnotation(MICRONAUT_CONTROLLER, args));

		ClassName delegatorClass = ClassName.get(getCurrentPackageName(), resourceDelegateName());

		FieldSpec fieldDelegate = FieldSpec.builder(delegatorClass, DELEGATE_VAR_NAME)
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
				.build();

		MethodSpec constructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(delegatorClass, DELEGATE_VAR_NAME)
				.addStatement("this.$N = $N", DELEGATE_VAR_NAME, DELEGATE_VAR_NAME)
				.addAnnotation(createAnnotation(JAVAX_INJECT))
				.build();

		resourceBuilder.addField(fieldDelegate);
		resourceBuilder.addMethod(constructor);

		generateResourceMethods();
		return resourceBuilder.build();
	}

	@Override
	public TypeSpec buildResourceImpl() {
		reset();
		setArtifactType(ArtifactType.RESOURCE_IMPL);
		TypeSpec.Builder builder = classBaseInstance(GeneratorUtil.createResourceDelegateName(getResourceContractContainer().getSourceFileName()));

		builder.addAnnotation(createAnnotation(JAVAX_SINGLETON));

		generateResourceMethods();
		return builder.build();
	}

	@Override
	public TypeName resourceMethodReturnType(Verb verb, Representation representation) {
		String v = toHttpMethod(verb);
		return GeneratorUtil.getMicronautReturnType(getResourceContractContainer().getSourceFileName(), v, verb.getVerb().endsWith("_COLLECTION"), getCurrentPackageName(), representation);
	}

	@Override
	public void generateMethodNotAllowedStatement(MethodSpec.Builder builder) {
		// not implemented
	}

	@Override
	public ClassName getMethodNowAllowedReturnType() {
		return MICRONAUT_HTTP_RESPONSE.getClassName();
	}

	@Override
	public AnnotationSpec getQueryParamAnnotation(VerbParameter param) {
		AnnotationSpec.Builder builder = AnnotationSpec.builder(MICRONAUT_QUERY_VALUE.getClassName())
				.addMember("value", "$S", param.getName());

		if (!param.getMandatory()) {
			builder.addMember("required", "$L", false);
		}
		return builder.build();
	}

	@Override
	public Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId, Representation representation, String methodName) {

		Set<AnnotationSpec> methodAnnotations = new HashSet<>();
		String httpMethod = getHttpMethod();

		Map<String, Object> annotationsFields = new HashMap<>();

		if (applyId) {
			if (representation.isJson() && !explicitExtensions) {
				annotationsFields.put("uri", "/{id}");
			} else {
				annotationsFields.put("uri", String.format("/{id}.%s", representation.getName()));
			}
		} else if(explicitExtensions) {
			annotationsFields.put("uri", String.format("/.%s", representation.getName()));
		}

		switch (httpMethod.toLowerCase()) {
			case "get":
				methodAnnotations.add(createAnnotation(MICRONAUT_GET, annotationsFields));
				break;
			case "options":
				methodAnnotations.add(createAnnotation(MICRONAUT_OPTIONS, annotationsFields));
				break;

			case "post":
				methodAnnotations.add(createAnnotation(MICRONAUT_POST, annotationsFields));
				break;
			case "put":
				methodAnnotations.add(createAnnotation(MICRONAUT_PUT, annotationsFields));
				break;
			case "delete":
				methodAnnotations.add(createAnnotation(MICRONAUT_DELETE, annotationsFields));
				break;
			default:
				throw new UnsupportedOperationException(String.format("Unsupported http method %s", httpMethod));
		}
		annotationsFields.clear();

		if (representation.isJson() && getResponseEncoding() != null) {
			annotationsFields.put("value", String.format("application/json;charset=%s", getResponseEncoding().name()));
		} else if (representation.isJson()) {
			annotationsFields.put("value", "application/json");
		} else {
			annotationsFields.put("value", representation.getMimetype());
		}
		methodAnnotations.add(createAnnotation(MICRONAUT_PRODUCES, annotationsFields));

		return methodAnnotations;
	}

	@Override
	public PluginTypes getPathVariableAnnotationType() {
		throw new UnsupportedOperationException("Micronaut does not have path variable annotations");
	}

	@Override
	public boolean supportsInterfaces() {
		return false;
	}

	@Override
	public boolean isHandlerMethod(String methodName) {
		return true;
	}

	@Override
	public AnnotationSpec buildRequestBodyAnnotation() {
		throw new UnsupportedOperationException("Micronaut does not have a request body annotation");
	}

	@Override
	public boolean supportsMethodNotAllowedGeneration() {
		return false;
	}

	@Override
	public boolean supportsQueryParams() {
		return false;
	}

	@Override
	public boolean supportsDelegation() {
		return true;
	}

	@Override
	public boolean inheritsFromResource() {
		return false;
	}
}
