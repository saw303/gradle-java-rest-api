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
package ch.silviowangler.gradle.restapi;

import ch.silviowangler.rest.model.CollectionModel;
import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.validation.PhoneNumber;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/** Created by Silvio Wangler on 26/01/16. */
public enum PluginTypes {
  RESTAPI_IDTYPE(ClassName.get("ch.silviowangler.rest.types", "IdType")),
  RESTAPI_FILTERMODEL(ClassName.get("ch.silviowangler.rest.model", "FilterModel")),
  RESTAPI_JWT_ANNOTATION(ClassName.get("ch.silviowangler.rest.security", "SecurityEnabled")),
  RESTAPI_CACHING_ANNOTATION(ClassName.get("ch.silviowangler.rest.cache", "CacheSetting")),
  RESTAPI_RESOURCE_MODEL(ClassName.get("ch.silviowangler.rest.model", "ResourceModel")),
  RESTAPI_IDENTIFIABLE(
      ParameterizedTypeName.get(
          ClassName.get("ch.silviowangler.rest.model", "Identifiable"),
          ClassName.get(String.class))),
  RESTAPI_RESPONSE_CREATOR(ClassName.get("ch.silviowangler.rest.micronaut", "ResponseCreator")),
  JAX_RS_RESPONSE(ClassName.get("javax.ws.rs.core", "Response")),
  JAX_RS_OPTIONS_VERB(ClassName.get("javax.ws.rs", "OPTIONS")),
  JAX_RS_GET_VERB(ClassName.get("javax.ws.rs", "GET")),
  JAX_RS_HEAD_VERB(ClassName.get("javax.ws.rs", "HEAD")),
  JAX_RS_POST_VERB(ClassName.get("javax.ws.rs", "POST")),
  JAX_RS_PUT_VERB(ClassName.get("javax.ws.rs", "PUT")),
  JAX_RS_DELETE_VERB(ClassName.get("javax.ws.rs", "DELETE")),
  JAX_RS_PRODUCES(ClassName.get("javax.ws.rs", "Produces")),
  JAX_RS_CONSUMES(ClassName.get("javax.ws.rs", "Consumes")),
  JAX_RS_PATH(ClassName.get("javax.ws.rs", "Path")),
  JAX_RS_FORM_PARAM(ClassName.get("javax.ws.rs", "FormParam")),
  JAX_RS_PATH_PARAM(ClassName.get("javax.ws.rs", "PathParam")),
  JAX_RS_QUERY_PARAM(ClassName.get("javax.ws.rs", "QueryParam")),
  JAX_RS_HEADER_PARAM(ClassName.get("javax.ws.rs", "HeaderParam")),
  JAX_RS_CONTEXT(ClassName.get("javax.ws.rs.core", "Context")),
  JAVAX_VALIDATION_SIZE(ClassName.get("javax.validation.constraints", "Size")),
  JAVAX_VALIDATION_DECIMAL_MIN(ClassName.get("javax.validation.constraints", "DecimalMin")),
  JAVAX_VALIDATION_DECIMAL_MAX(ClassName.get("javax.validation.constraints", "DecimalMax")),
  JAVAX_VALIDATION_MIN(ClassName.get("javax.validation.constraints", "Min")),
  JAVAX_VALIDATION_MAX(ClassName.get("javax.validation.constraints", "Max")),
  JAVAX_VALIDATION_NOT_NULL(ClassName.get("javax.validation.constraints", "NotNull")),
  JAVAX_VALIDATION_NOT_EMPTY(ClassName.get("javax.validation.constraints", "NotEmpty")),
  JAVAX_VALIDATION_EMAIL(ClassName.get("javax.validation.constraints", "Email")),
  JAVAX_VALIDATION_VALID(ClassName.get("javax.validation", "Valid")),
  JAVAX_GENERATED(ClassName.get("javax.annotation", "Generated")),
  JAVAX_NULLABLE(ClassName.get("javax.annotation", "Nullable")),
  JAVA_OVERRIDE(ClassName.get(Override.class)),
  PLUGIN_NOT_YET_IMPLEMENTED_EXCEPTION(
      ClassName.get("ch.silviowangler.rest", "NotYetImplementedException")),
  SPRING_REQUEST_MAPPING(
      ClassName.get("org.springframework.web.bind.annotation", "RequestMapping")),
  SPRING_REQUEST_PARAM(ClassName.get("org.springframework.web.bind.annotation", "RequestParam")),
  SPRING_REQUEST_HEADER(ClassName.get("org.springframework.web.bind.annotation", "RequestHeader")),
  SPRING_REQUEST_BODY(ClassName.get("org.springframework.web.bind.annotation", "RequestBody")),
  SPRING_PATH_VARIABLE(ClassName.get("org.springframework.web.bind.annotation", "PathVariable")),
  SPRING_REQUEST_METHOD(ClassName.get("org.springframework.web.bind.annotation", "RequestMethod")),
  SPRING_REST_CONTROLLER(
      ClassName.get("org.springframework.web.bind.annotation", "RestController")),
  SPRING_RESPONSE_ENTITY(ClassName.get("org.springframework.http", "ResponseEntity")),
  SPRING_HTTP_STATUS(ClassName.get("org.springframework.http", "HttpStatus")),
  SPRING_HTTP_MEDIA_TYPE(ClassName.get("org.springframework.http", "MediaType")),
  SPRING_RESPONSE_BODY(ClassName.get("org.springframework.web.bind.annotation", "ResponseBody")),
  SPRING_RESPONSE_STATUS(
      ClassName.get("org.springframework.web.bind.annotation", "ResponseStatus")),
  JAVAX_SINGLETON(ClassName.get("javax.inject", "Singleton")),
  JAVAX_INJECT(ClassName.get("javax.inject", "Inject")),
  /** Introduced with Micronaut 2.4. Will be default in Micronaut 3.0 and greater. */
  JAKARTA_SINGLETON(ClassName.get(Constants.PACKAGE_JAKARTA_INJECT, "Singleton")),
  /** Introduced with Micronaut 2.4. Will be default in Micronaut 3.0 and greater. */
  JAKARTA_INJECT(ClassName.get(Constants.PACKAGE_JAKARTA_INJECT, "Inject")),
  MICRONAUT_CLIENT(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_CLIENT_ANNOTATION, "Client")),
  MICRONAUT_CONTROLLER(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Controller")),
  MICRONAUT_STATUS(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Status")),
  MICRONAUT_VALIDATED(ClassName.get("io.micronaut.validation", "Validated")),
  MICRONAUT_CONSUMES(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Consumes")),
  MICRONAUT_PRODUCES(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Produces")),
  MICRONAUT_OPTIONS(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Options")),
  MICRONAUT_HEAD(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Head")),
  MICRONAUT_GET(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Get")),
  MICRONAUT_POST(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Post")),
  MICRONAUT_PUT(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Put")),
  MICRONAUT_DELETE(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Delete")),
  MICRONAUT_REQUEST_BODY(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Body")),
  MICRONAUT_HTTP_RESPONSE(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP, "HttpResponse")),
  MICRONAUT_HTTP_STATUS(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP, "HttpStatus")),
  MICRONAUT_HTTP_MEDIA_TYPE(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP, "MediaType")),
  MICRONAUT_QUERY_VALUE(
      ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "QueryValue")),
  MICRONAUT_HEADER(ClassName.get(Constants.PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION, "Header")),
  MICRONAUT_FORMAT(ClassName.get("io.micronaut.core.convert.format", "Format")),
  MICRONAUT_DATE_FORMAT(ClassName.get("ch.silviowangler.rest.micronaut.binding", "DateFormat")),
  MICRONAUT_INTROSPECTED(ClassName.get("io.micronaut.core.annotation", "Introspected")),
  MICRONAUT_DATE_TIME_FORMAT(
      ClassName.get("ch.silviowangler.rest.micronaut.binding", "DateTimeFormat")),
  MICRONAUT_EXECUTE_ON(ClassName.get("io.micronaut.scheduling.annotation", "ExecuteOn")),
  VALIDATION_PHONE_NUMBER(ClassName.get(PhoneNumber.class)),
  COLLECTION_MODEL(ClassName.get(CollectionModel.class)),
  ENTITY_MODEL(ClassName.get(EntityModel.class));

  private final TypeName typeName;

  PluginTypes(TypeName typeName) {
    this.typeName = typeName;
  }

  public TypeName getTypeName() {
    return this.typeName;
  }

  public ClassName getClassName() {

    if (this.typeName instanceof ClassName) return (ClassName) this.typeName;
    throw new UnsupportedOperationException(
        String.format(
            "TypeName %s is not a ClassName", this.typeName.getClass().getCanonicalName()));
  }

  private static class Constants {
    public static final String PACKAGE_JAKARTA_INJECT = "jakarta.inject";
    public static final String PACKAGE_IO_MICRONAUT_HTTP_ANNOTATION =
        "io.micronaut.http.annotation";
    public static final String PACKAGE_IO_MICRONAUT_HTTP_CLIENT_ANNOTATION =
        "io.micronaut.http.client.annotation";
    public static final String PACKAGE_IO_MICRONAUT_HTTP = "io.micronaut.http";
  }
}
