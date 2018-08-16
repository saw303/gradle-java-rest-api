/**
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
package ch.silviowangler.gradle.restapi.builder;

import ch.silviowangler.gradle.restapi.GeneratorUtil;
import ch.silviowangler.gradle.restapi.LinkParser;
import ch.silviowangler.gradle.restapi.tasks.GenerateRestApiTask;
import ch.silviowangler.rest.contract.model.v1.*;
import com.squareup.javapoet.*;
import io.github.getify.minify.Minify;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ch.silviowangler.gradle.restapi.PluginTypes.*;
import static ch.silviowangler.gradle.restapi.builder.ArtifactType.RESOURCE;
import static ch.silviowangler.gradle.restapi.util.SupportedDataTypes.*;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.*;

/**
 * @author Silvio Wangler
 */
public abstract class AbstractResourceBuilder implements ResourceBuilder {

	private TypeSpec.Builder typeBuilder;
	private ResourceContractContainer resourceContractContainer;
	private Verb currentVerb;
	private String currentPackageName;
	private boolean printTimestamp = true;
	private ArtifactType artifactType;
	private Charset responseEncoding;


	private Verb getCurrentVerb() {
		return currentVerb;
	}

	protected void setCurrentVerb(Verb currentVerb) {
		this.currentVerb = currentVerb;
	}

	@Override
	public String getCurrentPackageName() {
		return currentPackageName;
	}

	@Override
	public ArtifactType getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(ArtifactType artifactType) {
		this.artifactType = artifactType;
	}

	protected Charset getResponseEncoding() {
		return responseEncoding;
	}

	@Override
	public ResourceBuilder withResourceContractContainer(ResourceContractContainer resourceContract) {
		this.resourceContractContainer = resourceContract;
		return this;
	}

	@Override
	public ResourceBuilder withTimestampInGeneratedAnnotation(boolean val) {
		printTimestamp = val;
		return this;
	}

	@Override
	public ResourceBuilder withCurrentPackageName(String packageName) {
		this.currentPackageName = packageName;
		return this;
	}

	@Override
	public ResourceBuilder withResponseEncoding(Charset responseEncoding) {
		this.responseEncoding = responseEncoding;
		return this;
	}

	@Override
	public ResourceContractContainer getResourceContractContainer() {
		return this.resourceContractContainer;
	}


	protected TypeSpec.Builder resourceBaseTypeBuilder() {

		if (this.typeBuilder == null) {

			if (supportsInterfaces()) {
				this.typeBuilder = TypeSpec.interfaceBuilder(resourceName())
						.addModifiers(PUBLIC)
						.addAnnotation(createGeneratedAnnotation(printTimestamp));
			} else {
				this.typeBuilder = TypeSpec.classBuilder(resourceName())
						.addModifiers(PUBLIC, ABSTRACT)
						.addAnnotation(createGeneratedAnnotation(printTimestamp));
			}

		}
		return this.typeBuilder;
	}

	protected void reset() {
		this.artifactType = null;
		this.typeBuilder = null;
	}

	protected TypeSpec.Builder classBaseInstance() {

		if (this.typeBuilder == null) {
			this.typeBuilder = TypeSpec.classBuilder(resourceImplName()).addModifiers(PUBLIC);
		}
		return this.typeBuilder;
	}

	protected abstract void createOptionsMethod();

	@Override
	public void generateResourceMethods() {

		if (isAbstractOrInterfaceResource()) {

			String content = getResourceContractContainer().getResourceContractPlainText();
			content = Minify.minify(content).replaceAll("\"", "\\\\\"");

			FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(String.class), "OPTIONS_CONTENT").addModifiers(PUBLIC, STATIC, FINAL)
					.initializer("$N", "\"" + content + "\"");

			this.typeBuilder.addField(fieldBuilder.build());

			createOptionsMethod();
		}

		List<Verb> verbs = getResourceContractContainer().getResourceContract().getVerbs();
		verbs.sort(Comparator.comparing(Verb::getVerb));


		LinkParser parser = new LinkParser(getResourceContractContainer().getResourceContract().getGeneral().getxRoute(), getResourceContractContainer().getResourceContract().getGeneral().getVersion().split("\\.")[0]);

		for (Verb verb : verbs) {

			MethodSpec.Builder methodBuilder;

			this.currentVerb = verb;

			Map<String, VerbParameter> params = verb.getParameters().stream().collect(Collectors.toMap(VerbParameter::getName, Function.identity()));
			Map<String, TypeName> paramClasses = new HashMap<>();

			for (Representation representation : verb.getRepresentations()) {

				boolean directEntity = parser.isDirectEntity();

				List<ParameterSpec> pathParams = getPathParams(parser, isAbstractOrInterfaceResource());

				MethodContext context = new MethodContext(resourceMethodReturnType(verb, representation), params, paramClasses, representation, pathParams, directEntity);

				if (GenerateRestApiTask.GET_COLLECTION.equals(verb.getVerb())) {

					if (directEntity) {
						continue;
					}

					context.setMethodName("getCollection");
					methodBuilder = createMethod(context);

				} else if (GenerateRestApiTask.GET_ENTITY.equals(verb.getVerb())) {

					context.setMethodName("getEntity");
					methodBuilder = createMethod(context);

				} else {
					ClassName model = resourceModelName(verb);

					if (GenerateRestApiTask.POST.equals(verb.getVerb())) {

						paramClasses.put("model", model);

						context.setMethodName("createEntity");
						methodBuilder = createMethod(context);

					} else if (GenerateRestApiTask.PUT.equals(verb.getVerb()) || GenerateRestApiTask.PUT_ENTITY.equals(verb.getVerb())) {

						paramClasses.put("model", model);
						context.setMethodName("updateEntity");
						methodBuilder = createMethod(context);

					} else if (GenerateRestApiTask.PUT_COLLECTION.equals(verb.getVerb())) {

						paramClasses.put("model", ParameterizedTypeName.get(ClassName.get(Collection.class), model));
						context.setMethodName("updateEntities");
						methodBuilder = createMethod(context);

					} else if (GenerateRestApiTask.DELETE_COLLECTION.equals(verb.getVerb())) {

						context.setMethodName("deleteCollection");

						methodBuilder = createMethod(context);

					} else if (GenerateRestApiTask.DELETE_ENTITY.equals(verb.getVerb())) {

						context.setMethodName("deleteEntity");
						methodBuilder = createMethod(context);

					} else {
						throw new IllegalArgumentException(String.format("Verb %s is unknown", verb.getVerb()));
					}
				}

				MethodSpec resourceMethod = methodBuilder.build();

				if (!supportsInterfaces() && resourceMethod.modifiers.size() == 1 && resourceMethod.modifiers.contains(PUBLIC)) {

					context.setMethodName(String.format("handle%s", LOWER_CAMEL.to(UPPER_CAMEL, context.getMethodName())));
					context.setPathParams(getPathParams(parser, false));
					MethodSpec.Builder handlerBuilder = createMethod(context);
					this.typeBuilder.addMethod(handlerBuilder.build());
				}

				if (supportsInterfaces() || !isResourceImpl()) {
					this.typeBuilder.addMethod(resourceMethod);
				}
			}
			this.currentVerb = null;
		}


		if (isAbstractOrInterfaceResource()) {
			generatedDefaultMethodNotAllowedHandlersForMissingVerbs(parser.isDirectEntity());
		}
	}


	protected String getPath() {
		GeneralDetails general = getResourceContractContainer().getResourceContract().getGeneral();
		return new LinkParser(general.getxRoute(), general.getVersion().split("\\.")[0]).toBasePath();
	}

	protected String getHttpMethod() {
		return toHttpMethod(getCurrentVerb()).toUpperCase();
	}

	@Override
	public Set<TypeSpec> buildResourceTypes(Set<ClassName> types, String packageName) {

		ResourceContract resourceContract = getResourceContractContainer().getResourceContract();
		List<ResourceTypes> contractTypes = resourceContract.getTypes();
		Set<TypeSpec> specTypes = new HashSet<>(types.size());

		for (ResourceTypes type : contractTypes) {
			TypeSpec.Builder builder = resourceTypeBaseInstance(type.getName());

			for (ResourceTypeField field : type.getFields()) {

				TypeName fieldType = getFieldType(types, field.getType());

				if ("true".equals(field.getMultiple())) {
					ClassName list = ClassName.get(List.class);
					fieldType = ParameterizedTypeName.get(list, fieldType);
				}

				builder.addField(FieldSpec.builder(fieldType, field.getName(), PRIVATE).build());

				// write Getter/Setters
				writeGetterSetter(builder, fieldType, field.getName());
			}
			TypeSpec typeSpec = builder.build();
			types.add(ClassName.get(packageName, typeSpec.name));
			specTypes.add(typeSpec);
		}
		return specTypes;
	}


	@Override
	public Set<TypeSpec> buildResourceModels(Set<ClassName> types) {
		ResourceContract resourceContract = getResourceContractContainer().getResourceContract();

		List<Verb> verbs;
		List<Verb> declaredVerbs = resourceContract.getVerbs();
		if (declaredVerbs.size() == 1 && declaredVerbs.get(0).getVerb().equals(GET_COLLECTION)) {
			verbs = declaredVerbs;
		} else {

			List<String> excludeVerbs = new ArrayList<>();
			excludeVerbs.add(DELETE_ENTITY);

			Optional<Verb> getEntity = declaredVerbs.stream().filter(verb -> verb.getVerb().equals(GET_ENTITY)).findAny();

			if (getEntity.isPresent()) {
				excludeVerbs.add(GET_COLLECTION);
			}

			verbs = declaredVerbs.stream().filter(v -> !excludeVerbs.contains(v.getVerb())).collect(Collectors.toList());
		}
		Set<TypeSpec> specTypes = new HashSet<>(verbs.size());


		Verb verbGet = verbs.stream().filter(v -> v.getVerb().equals(GET_ENTITY)).findAny().orElse(verbs.get(0));

		List<ResourceField> fields = resourceContract.getFields();

		for (Verb verb : verbs) {

			List<String> fieldNamesApplied = new ArrayList<>();

			ClassName resourceModelName = resourceModelName(verb);
			TypeSpec.Builder builder = resourceModelBaseInstance(verb);
			String mimeType = verbGet.getRepresentations().stream().filter(r -> "json".equals(r.getName())).findAny().get().getMimetype();

			builder.addField(
					FieldSpec.builder(String.class, "TYPE")
							.addModifiers(PUBLIC, FINAL, STATIC)
							.initializer("$S", mimeType)
							.build()
			);

			for (ResourceField field : fields) {

				if (!field.isVisible() && verb.equals(verbGet)) continue;
				if (field.isReadonly() && !verb.equals(verbGet)) continue;

				fieldNamesApplied.add(field.getName());

				TypeName fieldType = getFieldType(types, field.getType());

				if (field.isMultiple()) {
					ClassName list = ClassName.get(List.class);
					fieldType = ParameterizedTypeName.get(list, fieldType);
				}

				FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, field.getName(), PRIVATE);

				if (field.getMandatory().stream().filter(v -> v.equalsIgnoreCase(verb.getVerb())).findAny().isPresent()) {
					fieldBuilder.addAnnotation(createAnnotation(JAVAX_VALIDATION_NOT_NULL));
				}

				if (!verb.equals(verbGet) && field.getMin() instanceof Number && field.getMax() instanceof Number) {

					Number min = field.getMin();
					Number max = field.getMax();

					if (field.getType().equalsIgnoreCase("string")) {
						int minInt = min.intValue();
						int maxInt = max.intValue();
						fieldBuilder.addAnnotation(
								AnnotationSpec.builder(JAVAX_VALIDATION_SIZE.getClassName())
										.addMember("min", "$L", String.valueOf(minInt))
										.addMember("max", "$L", String.valueOf(maxInt)).build()
						);
					}
					if (field.getType().equalsIgnoreCase("decimal")) {
						fieldBuilder.addAnnotation(
								AnnotationSpec.builder(JAVAX_VALIDATION_DECIMAL_MIN.getClassName())
										.addMember("value", "$S", min.doubleValue()).build()
						);
						fieldBuilder.addAnnotation(
								AnnotationSpec.builder(JAVAX_VALIDATION_DECIMAL_MAX.getClassName())
										.addMember("value", "$S", max.doubleValue()).build()
						);
					}

				}


				if (field.isMultiple()) {
					fieldBuilder.initializer("new java.util.ArrayList<>()");
				} else if (field.getDefaultValue() != null) {

					if (fieldType == STRING.getClassName()) {
						fieldBuilder.initializer("$S", field.getDefaultValue());
					} else if (fieldType == DATE.getClassName()) {
						fieldBuilder.initializer("$T.now()", ClassName.get(LocalDate.class));
					} else if (fieldType == DATETIME.getClassName()) {
						fieldBuilder.initializer("$T.now()", ClassName.get(LocalDateTime.class));
					} else if (fieldType == BOOL.getClassName()) {
						fieldBuilder.initializer("$T.$L", ClassName.get(Boolean.class), Boolean.TRUE.equals(field.getDefaultValue()) ? "TRUE" : "FALSE");
					} else {
						fieldBuilder.initializer("$S", field.getDefaultValue());
					}
				}
				builder.addField(fieldBuilder.build());

				// write Getter/Setters
				writeGetterSetter(builder, fieldType, field.getName());
			}

			// --> overwrite equals method
			String equalsParamName = "other";
			String equalsCastVarName = "that";

			MethodSpec.Builder equalsBuilder = MethodSpec.methodBuilder("equals")
					.addAnnotation(Override.class)
					.addModifiers(PUBLIC).addParameter(Object.class, equalsParamName).returns(TypeName.BOOLEAN);

			equalsBuilder.addStatement("if (this == $L) return true", equalsParamName)
					.addStatement("if (! ($L instanceof $T)) return false", equalsParamName, resourceModelName)
					.addStatement("$T $L = ($T) $L", resourceModelName, equalsCastVarName, resourceModelName, equalsParamName);


			String code = fieldNamesApplied.stream()
					.map(f -> "get" + LOWER_CAMEL.to(UPPER_CAMEL, f))
					.map(f -> "$T.equals(" + f + "(), " + equalsCastVarName + "." + f + "())")
					.collect(Collectors.joining(" && "));

			equalsBuilder.addStatement("return " + code, Collections.nCopies(fieldNamesApplied.size(), Objects.class).toArray());

			builder.addMethod(equalsBuilder.build());

			// --> overwrite hashCode method
			MethodSpec.Builder hashCodeBuilder = MethodSpec.methodBuilder("hashCode")
					.addAnnotation(Override.class)
					.addModifiers(PUBLIC).returns(INT);

			code = "$T.hash(" + fieldNamesApplied.stream().collect(Collectors.joining(", ")) + ")";

			hashCodeBuilder.addStatement("return " + code, Objects.class);

			builder.addMethod(hashCodeBuilder.build());

			specTypes.add(builder.build());
		}
		return specTypes;
	}

	private TypeName getFieldType(Set<ClassName> types, String fieldType) {
		TypeName type;
		try {
			type = GeneratorUtil.translateToJava(fieldType);
		} catch (Exception e) {
			Optional<ClassName> any = types.stream().filter(t -> t.simpleName().equalsIgnoreCase(fieldType + "Type")).findAny();

			if (!any.isPresent()) {
				throw e;
			}
			type = any.get();
		}
		return type;
	}

	private void writeGetterSetter(TypeSpec.Builder builder, TypeName fieldType, String name) {
		String methodName = LOWER_CAMEL.to(UPPER_CAMEL, name);
		MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder("get" + methodName)
				.returns(fieldType).addModifiers(PUBLIC).addStatement("return this.$L", name);

		builder.addMethod(getterBuilder.build());
		MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder("set" + methodName)
				.returns(TypeName.VOID)
				.addModifiers(PUBLIC)
				.addParameter(ParameterSpec.builder(fieldType, name).build())
				.addStatement("this.$L = $L", name, name);
		builder.addMethod(setterBuilder.build());
	}

	private boolean isResourceInterface() {
		return RESOURCE.equals(getArtifactType());
	}

	private boolean isAbstractResourceInterface() {
		return ArtifactType.ABSTRACT_RESOURCE.equals(getArtifactType());
	}

	private boolean isAbstractOrInterfaceResource() {
		return isAbstractResourceInterface() || isResourceInterface();
	}

	private boolean isResourceImpl() {
		return ArtifactType.RESOURCE_IMPL.equals(getArtifactType());
	}

	private void generatedDefaultMethodNotAllowedHandlersForMissingVerbs(boolean directEntity) {


		if (!hasPostVerb()) {
			this.currentVerb = new Verb(POST);
			this.typeBuilder.addMethod(createMethodNotAllowedHandler("createEntityAutoAnswer").build());
		}

		if (!hasDeleteCollectionVerb()) {
			this.currentVerb = new Verb(DELETE_COLLECTION);
			this.typeBuilder.addMethod(createMethodNotAllowedHandler("deleteCollectionAutoAnswer").build());
		}

		if (!hasDeleteEntityVerb()) {
			this.currentVerb = new Verb(DELETE_ENTITY);
			this.typeBuilder.addMethod(createMethodNotAllowedHandler("deleteEntityAutoAnswer").build());
		}

		if (!hasGetCollectionVerb() && !directEntity) {
			this.currentVerb = new Verb(GET_COLLECTION);
			this.typeBuilder.addMethod(createMethodNotAllowedHandler("getCollectionAutoAnswer").build());
		} else if (!hasGetEntityVerb()) {
			this.currentVerb = new Verb(GET_ENTITY);
			this.typeBuilder.addMethod(createMethodNotAllowedHandler("getEntityAutoAnswer").build());
		}

		if (!hasPutVerb()) {
			this.currentVerb = new Verb(PUT);
			this.typeBuilder.addMethod(createMethodNotAllowedHandler("updateEntityAutoAnswer").build());
		}
		this.currentVerb = null;
	}

	private boolean hasGetEntityVerb() {
		return hasVerb(GET_ENTITY);
	}

	private boolean hasGetCollectionVerb() {
		return hasVerb(GET_COLLECTION);
	}

	private boolean hasPostVerb() {
		return hasVerb(POST);
	}

	private boolean hasPutVerb() {
		return hasVerb(PUT);
	}

	private boolean hasDeleteCollectionVerb() {
		return hasVerb(DELETE_COLLECTION);
	}

	private boolean hasDeleteEntityVerb() {
		return hasVerb(DELETE_ENTITY);
	}

	private boolean hasVerb(String verb) {
		return getResourceContractContainer().getResourceContract().getVerbs().stream().filter(v -> verb.equals(v.getVerb())).findAny().isPresent();
	}

	private TypeSpec.Builder resourceTypeBaseInstance(String name) {
		TypeSpec.Builder builder = TypeSpec
				.classBuilder(LOWER_CAMEL.to(UPPER_CAMEL, name) + "Type")
				.addModifiers(PUBLIC)
				.addSuperinterface(ClassName.get(Serializable.class));
		return this.typeBuilder = builder;
	}

	private TypeSpec.Builder resourceModelBaseInstance(Verb verb) {
		TypeSpec.Builder builder = TypeSpec.classBuilder(resourceModelName(verb))
				.addModifiers(PUBLIC)
				.addAnnotation(createGeneratedAnnotation(printTimestamp))
				.addSuperinterface(Serializable.class)
				.addSuperinterface(RESTAPI_RESOURCE_MODEL.getClassName());

		return builder;
	}

	private List<ParameterSpec> getPathParams(LinkParser parser, boolean addAnnotations) {
		List<ParameterSpec> pathParams = new ArrayList<>(parser.getPathVariables().size());

		for (String pathVar : parser.getPathVariables()) {
			ParameterSpec.Builder paramBuilder = ParameterSpec.builder(String.class, pathVar);

			if (addAnnotations) {
				paramBuilder.addAnnotation(
						AnnotationSpec.builder(getPathVariableAnnotationType().getClassName())
								.addMember("value", "$S", pathVar)
								.build()
				);
			}
			pathParams.add(paramBuilder.build());
		}
		return pathParams;
	}
}
