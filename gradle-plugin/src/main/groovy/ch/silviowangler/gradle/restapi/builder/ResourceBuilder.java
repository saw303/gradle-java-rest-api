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
package ch.silviowangler.gradle.restapi.builder;

import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.PluginTypes;
import ch.silviowangler.gradle.restapi.RestApiPlugin;
import ch.silviowangler.rest.contract.model.v1.Representation;
import ch.silviowangler.rest.contract.model.v1.Verb;
import ch.silviowangler.rest.contract.model.v1.VerbParameter;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;

import java.nio.charset.Charset;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static ch.silviowangler.gradle.restapi.PluginTypes.*;
import static javax.lang.model.element.Modifier.*;

public interface ResourceBuilder {

	String GET_COLLECTION = "GET_COLLECTION";
	String GET_ENTITY = "GET_ENTITY";
	String HEAD_COLLECTION = "HEAD_COLLECTION";
	String HEAD_ENTITY = "HEAD_ENTITY";
	String POST = "POST";
	String PUT = "PUT";
	String PUT_ENTITY = "PUT_ENTITY";
	String PUT_COLLECTION = "PUT_COLLECTION";
	String DELETE_ENTITY = "DELETE_ENTITY";
	String DELETE_COLLECTION = "DELETE_COLLECTION";
	String OPTIONS = "OPTIONS";

	List<String> GET_METHODS = Arrays.asList(GET_ENTITY, GET_COLLECTION);
	List<String> HEAD_METHODS = Arrays.asList(HEAD_ENTITY, HEAD_COLLECTION);
	List<String> DELETE_METHODS = Arrays.asList(DELETE_ENTITY, DELETE_COLLECTION);
	List<String> PUT_METHODS = Arrays.asList(PUT, PUT_ENTITY, PUT_COLLECTION);

	String getCurrentPackageName();

	ResourceBuilder withCurrentPackageName(String packageName);

	ResourceBuilder withResponseEncoding(Charset responseEncoding);

	ArtifactType getArtifactType();

	ResourceContractContainer getResourceContractContainer();

	TypeSpec buildResource();

	TypeSpec buildResourceImpl();

	default String resourceName() {
		return GeneratorUtil.createResourceName(getResourceContractContainer().getSourceFileName());
	}

	default String resourceImplName() {
		return GeneratorUtil.createResourceImplementationName(getResourceContractContainer().getSourceFileName());
	}

	default String resourceDelegateName() {
		return GeneratorUtil.createResourceDelegateName(getResourceContractContainer().getSourceFileName());
	}


	TypeName resourceMethodReturnType(Verb verb, Representation representation);

	default String toHttpMethod(Verb verb) {
		String verbName = verb.getVerb();
		String v;

		if (GET_METHODS.contains(verbName)) {
			v = "Get";
		} else if (DELETE_METHODS.contains(verbName)) {
			v = "Delete";
		} else if (HEAD_METHODS.contains(verbName)) {
			v = "Head";
		} else if (PUT_METHODS.contains(verbName)) {
			v = "Put";
		} else if (POST.equals(verbName)) {
			v = "Post";
		} else if (OPTIONS.equals(verbName)) {
			v = "Options";
		} else {
			throw new IllegalArgumentException("Unknown verb " + verbName);
		}
		return v;
	}

	default ClassName resourceModelName(Verb verb) {
		return ClassName.get(getCurrentPackageName(), GeneratorUtil.createResourceModelName(getResourceContractContainer().getSourceFileName(), toHttpMethod(verb)));
	}

	default AnnotationSpec createGeneratedAnnotation(boolean printTimestamp) {

		Map<String, Object> map = new HashMap<>();

		map.put("value", RestApiPlugin.PLUGIN_ID);
		map.put("comments", "Specification filename: " + getResourceContractContainer().getSourceFileName());

		if (printTimestamp) {
			ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
			map.put("date", utc.toString());
		}

		return createAnnotation(JAVAX_GENERATED, map);
	}

	default AnnotationSpec createAnnotation(PluginTypes className) {
		return createAnnotation(className, new HashMap<>());
	}

	default AnnotationSpec createAnnotation(PluginTypes className, Map<String, Object> attributes) {
		AnnotationSpec.Builder builder = AnnotationSpec.builder(className.getClassName());

		for (Map.Entry<String, Object> entry : attributes.entrySet()) {

			String param = "$S";

			if (entry.getValue() instanceof String && ((String) entry.getValue()).endsWith(".class")) {
				param = "$N";
			}
			builder.addMember(entry.getKey(), param, entry.getValue());
		}
		return builder.build();
	}

	void generateResourceMethods();

	void generateMethodNotAllowedStatement(MethodSpec.Builder builder);

	ClassName getMethodNowAllowedReturnType();

	MethodSpec.Builder createMethodNotAllowedHandler(String methodName);

	MethodSpec.Builder createMethod(String methodName, TypeName returnType);

	default MethodSpec.Builder createMethod(MethodContext context) {

		String methodName = context.getMethodName();
		Representation representation = context.getRepresentation();

		if (!representation.isJson()) {
			methodName += CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, representation.getName());
		}
		final String methodNameCopy = String.valueOf(methodName);

		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC);
		methodBuilder.returns(context.getReturnType());

		final ArtifactType artifactType = getArtifactType();
		final boolean isResourceInterface = ArtifactType.RESOURCE.equals(artifactType);
		final boolean isAbstractResourceClass = ArtifactType.ABSTRACT_RESOURCE.equals(artifactType);
		final boolean isDelegateResourceClass = ArtifactType.DELEGATOR_RESOURCE.equals(artifactType);

		if (isResourceInterface || isDelegateResourceClass || (isAbstractResourceClass && !isHandlerMethod(methodName))) {
			Iterable<AnnotationSpec> annotations = getResourceMethodAnnotations(isIdGenerationRequired(context), representation, methodName);
			methodBuilder.addAnnotations(annotations);

		} else if (ArtifactType.RESOURCE_IMPL.equals(artifactType) && inheritsFromResource()) {
			methodBuilder.addAnnotation(AnnotationSpec.builder(JAVA_OVERRIDE.getClassName()).build());
		}

		boolean generateIdParamAnnotation = false;

		if (!supportsInterfaces() && isHandlerMethod(methodName) && ArtifactType.RESOURCE_IMPL.equals(getArtifactType())) {
			methodBuilder.addStatement("throw new $T()", PLUGIN_NOT_YET_IMPLEMENTED_EXCEPTION.getClassName());
		} else if (isDelegateResourceClass) {
			// do nothing
		} else if ("getOptions".equals(methodName) || isDefaultMethodNotAllowed(methodName)) {

			if (isResourceInterface) {
				methodBuilder.addModifiers(DEFAULT);
			}

			generateIdParamAnnotation = true;
		} else if (!supportsInterfaces() && !isHandlerMethod(methodName)) {
			generateIdParamAnnotation = true;
		} else if (!supportsInterfaces() && isHandlerMethod(methodName) && ArtifactType.ABSTRACT_RESOURCE.equals(getArtifactType())) {
			methodBuilder.addModifiers(ABSTRACT);
		} else {
			if (isResourceInterface) {
				methodBuilder.addModifiers(ABSTRACT);
				generateIdParamAnnotation = true;
			} else {
				methodBuilder.addStatement("throw new $T()", PLUGIN_NOT_YET_IMPLEMENTED_EXCEPTION.getClassName());
			}
		}

		List<String> names = new ArrayList<>(context.getParams().size());

		context.getParams().forEach(p -> {

			ParameterSpec.Builder builder = ParameterSpec.builder(GeneratorUtil.translateToJava(p.getType()), p.getName());

			final boolean isHandleMethod = methodNameCopy.startsWith("handle");
			final boolean isResource = isResourceInterface || isAbstractResourceClass || isDelegateResourceClass;

			if (isResource && !isHandleMethod) {
				for (AnnotationSpec queryParamAnnotation : getQueryParamAnnotations(p)) {
					builder.addAnnotation(queryParamAnnotation);
				}
			}

			ParameterSpec parameter = builder.build();

			methodBuilder.addParameter(parameter);
			names.add(p.getName());
		});

		context.getParamClasses().forEach((name, className) -> {

			ParameterSpec.Builder builder = ParameterSpec.builder(className, name);

			final boolean isHandleMethod = methodNameCopy.startsWith("handle");
			final boolean isResource = isResourceInterface || isAbstractResourceClass || isDelegateResourceClass;

			if ("model".equals(name) && !isHandleMethod && isResource) {
				builder.addAnnotation(createAnnotation(JAVAX_VALIDATION_VALID)).build();

				if (providesRequestBodyAnnotation()) {
					builder.addAnnotation(buildRequestBodyAnnotation());
				}
			}

			ParameterSpec parameter = builder.build();

			methodBuilder.addParameter(parameter);
			names.add(name);
		});

		if (!context.isDirectEntity() && methodName.matches("(handle)?(get|head|update|delete|Get|Head|Update|Delete)Entity.*")) {
			ParameterSpec id = generateIdParam(generateIdParamAnnotation);
			methodBuilder.addParameter(id);
			names.add(id.name);
		}

		context.getPathParams().forEach(p -> {
			names.add(p.name);
			methodBuilder.addParameter(p);
		});

		String paramNames = String.join(", ", names);

		if (methodName.startsWith("head") && isDelegateResourceClass) {
			addHeadStatement(methodBuilder, context, paramNames);
		} else if (!supportsInterfaces() && !isHandlerMethod(methodName) && ArtifactType.ABSTRACT_RESOURCE.equals(artifactType) && !methodName.endsWith("AutoAnswer") && !"getOptions".equals(methodName)) {
			methodBuilder.addStatement("return handle$L($L)", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, methodName), paramNames);
		} else if (isDelegateResourceClass && !methodName.equals("getOptions")) {
			if (TypeName.VOID.equals(context.getReturnType())) {
				methodBuilder.addStatement("delegate.$L($L)", methodName, paramNames);
			} else {
				methodBuilder.addStatement("return delegate.$L($L)", methodName, paramNames);
			}
		}
		return methodBuilder;
	}

	default void addHeadStatement(MethodSpec.Builder methodBuilder, MethodContext context, String params) {
		throw new UnsupportedOperationException("Head method generation is not supported for the selected framework");
	}

	default boolean isHandlerMethod(String methodName) {
		return methodName.startsWith("handle");
	}

	default boolean isIdGenerationRequired(MethodContext context) {
		List<String> noId = Arrays.asList("getOptions", "createEntity", "getCollection", "headCollection", "deleteCollection");

		if (context.isDirectEntity()) return false;

		for (String s : noId) {
			if (context.getMethodName().startsWith(s)) {
				return false;
			}
		}
		return true;
	}

	default boolean isDefaultMethodNotAllowed(String methodName) {
		return methodName.endsWith("AutoAnswer") && ArtifactType.RESOURCE.equals(getArtifactType());
	}

	List<AnnotationSpec> getQueryParamAnnotations(VerbParameter paramName);

	Iterable<AnnotationSpec> getResourceMethodAnnotations(boolean applyId, Representation representation, String methodName);

	PluginTypes getPathVariableAnnotationType();

	default ParameterSpec generateIdParam(boolean withAnnotation) {
		ParameterSpec.Builder param = ParameterSpec.builder(ClassName.get(String.class), "id");

		if (withAnnotation && supportsQueryParams()) {

			Map<String, Object> attrs = new HashMap<>();
			attrs.put("value", "id");

			AnnotationSpec annotation = createAnnotation(getPathVariableAnnotationType(), attrs);
			param.addAnnotation(annotation).build();
		}
		return param.build();
	}

	ResourceBuilder withResourceContractContainer(ResourceContractContainer resourceContract);

	ResourceBuilder withTimestampInGeneratedAnnotation(boolean val);

	Set<TypeSpec> buildResourceTypes(Set<ClassName> types, String packageName);

	Set<TypeSpec> buildResourceModels(Set<ClassName> types);

	/**
	 * Some frameworks such as Spring already provide a HEAD method implicitly for every GET method.
	 * However frameworks like e.g. Micronaut need to have such a HEAD method generated explicitly.
	 *
	 * @return yes or no
	 */
	boolean shouldGenerateHeadMethod();

	boolean supportsInterfaces();

	default boolean inheritsFromResource() {
		return true;
	}

	/**
	 * Does the framework work with delegation. the request will delegated from a controller to a delegate resource.
	 *
	 * @return yes or no
	 */
	default boolean supportsDelegation() {
		return false;
	}

	/**
	 * Declares whether the framework of this builder supports Java annotations for HTTP request params.
	 *
	 * @return yes or no
	 */
	default boolean supportsQueryParams() {
		return true;
	}

	default boolean providesRequestBodyAnnotation() {
		return false;
	}

	AnnotationSpec buildRequestBodyAnnotation();

	default boolean supportsMethodNotAllowedGeneration() {
		return true;
	}
}
