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

class LinkParser {

	String link
	String version
	List<String> pathVariables = []
	boolean directEntity


	LinkParser(String link, String version) {
		this.directEntity = !link.contains(':entity')
		this.link = link.replace(':version', "v${version}").replace('/:entity', '')
		this.version = version

		if (this.link.contains(':')) {
			initializePathVariables()
		}
	}

	private void initializePathVariables() {

		def finder = (this.link =~ /:[a-z]*/)

		for (token in finder) {
			pathVariables << "${token[1..token.length() - 1]}".toString()
		}

		for (String pathVar in pathVariables) {
			this.link = this.link.replaceAll(":${pathVar}/", "{${pathVar}}/")
		}
	}

	String toBasePath() {
		link
	}

	String toEntityPath() {
		'{id}'
	}
}
