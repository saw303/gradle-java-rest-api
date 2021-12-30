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
package ch.silviowangler.gradle.restapi.builder

import ch.silviowangler.gradle.restapi.LinkParser
import ch.silviowangler.rest.contract.model.v1.Header
import ch.silviowangler.rest.contract.model.v1.Representation
import ch.silviowangler.rest.contract.model.v1.VerbParameter
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import groovy.transform.ToString

/**
 * @author Silvio Wangler
 */
@ToString
class MethodContext {

	String methodName
	TypeName returnType
	List<VerbParameter> params = []
	List<Header> headers = []
	Map<String, TypeName> paramClasses = [:]
	Representation representation
	List<ParameterSpec> pathParams = []
	boolean directEntity
	LinkParser linkParser
	boolean expandable = false

	MethodContext(String methodName, TypeName returnType, Representation representation) {
		this.methodName = methodName
		this.returnType = returnType
		this.representation = representation
	}

	MethodContext(TypeName returnType, List<VerbParameter> params, List<Header> headers, Map<String, TypeName> paramClasses, Representation representation, List<ParameterSpec> pathParams, LinkParser linkParser) {
		this.returnType = returnType
		this.params = params
		this.representation = representation
		this.pathParams = pathParams
		this.directEntity = linkParser.directEntity
		this.paramClasses = paramClasses
		this.headers = headers
		this.linkParser = linkParser
	}
}
