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
package ch.silviowangler.gradle.restapi;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * Created by Silvio Wangler on 26/01/16.
 */
public enum PluginTypes {

	RESTAPI_IDTYPE(ClassName.get("ch.silviowangler.rest.types", "IdType")),
	RESTAPI_FILTERMODEL(ClassName.get("ch.silviowangler.rest.model", "FilterModel")),
	RESTAPI_JWT_ANNOTATION(ClassName.get("ch.silviowangler.rest.security", "SecurityEnabled")),
	RESTAPI_CACHING_ANNOTATION(ClassName.get("ch.silviowangler.rest.cache", "CacheSetting")),
	RESTAPI_RESOURCE_MODEL(ClassName.get("ch.silviowangler.rest.model", "ResourceModel")),
	RESTAPI_IDENTIFIABLE(ParameterizedTypeName.get(ClassName.get("ch.silviowangler.rest.model", "Identifiable"), ClassName.get(String.class))),
	JAX_RS_RESPONSE(ClassName.get("javax.ws.rs.core", "Response")),
	JAX_RS_OPTIONS_VERB(ClassName.get("javax.ws.rs", "OPTIONS")),
	JAX_RS_GET_VERB(ClassName.get("javax.ws.rs", "GET")),
	JAX_RS_POST_VERB(ClassName.get("javax.ws.rs", "POST")),
	JAX_RS_PUT_VERB(ClassName.get("javax.ws.rs", "PUT")),
	JAX_RS_DELETE_VERB(ClassName.get("javax.ws.rs", "DELETE")),
	JAX_RS_PRODUCES(ClassName.get("javax.ws.rs", "Produces")),
	JAX_RS_CONSUMES(ClassName.get("javax.ws.rs", "Consumes")),
	JAX_RS_PATH(ClassName.get("javax.ws.rs", "Path")),
	JAX_RS_FORM_PARAM(ClassName.get("javax.ws.rs", "FormParam")),
	JAX_RS_PATH_PARAM(ClassName.get("javax.ws.rs", "PathParam")),
	JAX_RS_QUERY_PARAM(ClassName.get("javax.ws.rs", "QueryParam")),
	JAX_RS_CONTEXT(ClassName.get("javax.ws.rs.core", "Context")),
	JAVAX_VALIDATION_SIZE(ClassName.get("javax.validation.constraints", "Size")),
	JAVAX_VALIDATION_DECIMAL_MIN(ClassName.get("javax.validation.constraints", "DecimalMin")),
	JAVAX_VALIDATION_DECIMAL_MAX(ClassName.get("javax.validation.constraints", "DecimalMax")),
	JAVAX_VALIDATION_NOT_NULL(ClassName.get("javax.validation.constraints", "NotNull")),
	JAVAX_VALIDATION_EMAIL(ClassName.get("javax.validation.constraints", "Email")),
	JAVAX_VALIDATION_VALID(ClassName.get("javax.validation", "Valid")),
	JAVAX_GENERATED(ClassName.get("javax.annotation", "Generated")),
	JAVAX_NULLABLE(ClassName.get("javax.annotation", "Nullable")),
	JAVA_OVERRIDE(ClassName.get(Override.class)),
	JAVA_VOID(ClassName.get(Void.class)),
	PLUGIN_NOT_YET_IMPLEMENTED_EXCEPTION(ClassName.get("ch.silviowangler.rest", "NotYetImplementedException")),
	SPRING_REQUEST_MAPPING(ClassName.get("org.springframework.web.bind.annotation", "RequestMapping")),
	SPRING_REQUEST_PARAM(ClassName.get("org.springframework.web.bind.annotation", "RequestParam")),
	SPRING_REQUEST_BODY(ClassName.get("org.springframework.web.bind.annotation", "RequestBody")),
	SPRING_PATH_VARIABLE(ClassName.get("org.springframework.web.bind.annotation", "PathVariable")),
	SPRING_REQUEST_METHOD(ClassName.get("org.springframework.web.bind.annotation", "RequestMethod")),
	SPRING_REST_CONTROLLER(ClassName.get("org.springframework.web.bind.annotation", "RestController")),
	SPRING_RESPONSE_ENTITY(ClassName.get("org.springframework.http", "ResponseEntity")),
	SPRING_HTTP_STATUS(ClassName.get("org.springframework.http", "HttpStatus")),
	SPRING_HTTP_MEDIA_TYPE(ClassName.get("org.springframework.http", "MediaType")),
	SPRING_RESPONSE_BODY(ClassName.get("org.springframework.web.bind.annotation", "ResponseBody")),
	SPRING_RESPONSE_STATUS(ClassName.get("org.springframework.web.bind.annotation", "ResponseStatus")),
	JAVAX_SINGLETON(ClassName.get("javax.inject", "Singleton")),
	JAVAX_INJECT(ClassName.get("javax.inject", "Inject")),
	MICRONAUT_CONTROLLER(ClassName.get("io.micronaut.http.annotation", "Controller")),
	MICRONAUT_STATUS(ClassName.get("io.micronaut.http.annotation", "Status")),
	MICRONAUT_VALIDATED(ClassName.get("io.micronaut.validation", "Validated")),
	MICRONAUT_CONSUMES(ClassName.get("io.micronaut.http.annotation", "Consumes")),
	MICRONAUT_PRODUCES(ClassName.get("io.micronaut.http.annotation", "Produces")),
	MICRONAUT_OPTIONS(ClassName.get("io.micronaut.http.annotation", "Options")),
	MICRONAUT_GET(ClassName.get("io.micronaut.http.annotation", "Get")),
	MICRONAUT_POST(ClassName.get("io.micronaut.http.annotation", "Post")),
	MICRONAUT_PUT(ClassName.get("io.micronaut.http.annotation", "Put")),
	MICRONAUT_DELETE(ClassName.get("io.micronaut.http.annotation", "Delete")),
	MICRONAUT_HTTP_RESPONSE(ClassName.get("io.micronaut.http", "HttpResponse")),
	MICRONAUT_QUERY_VALUE(ClassName.get("io.micronaut.http.annotation", "QueryValue")),
	MICRONAUT_FORMAT(ClassName.get("io.micronaut.core.convert.format", "Format")),
	MICRONAUT_HTTP_STATUS(ClassName.get("io.micronaut.http", "HttpStatus")),
	MICRONAUT_DATE_FORMAT(ClassName.get("ch.silviowangler.rest.micronaut.binding", "DateFormat")),
	MICRONAUT_DATE_TIME_FORMAT(ClassName.get("ch.silviowangler.rest.micronaut.binding", "DateTimeFormat"));

	private final TypeName typeName;

	PluginTypes(TypeName typeName) {
		this.typeName = typeName;
	}

	public TypeName getTypeName() {
		return this.typeName;
	}

	public ClassName getClassName() {

		if (this.typeName instanceof ClassName) return (ClassName) this.typeName;
		throw new UnsupportedOperationException(String.format("TypeName %s is not a ClassName", this.typeName.getClass().getCanonicalName()));
	}
}
