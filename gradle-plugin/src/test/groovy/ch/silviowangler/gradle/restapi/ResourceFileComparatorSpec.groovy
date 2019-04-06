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

import spock.lang.Specification

class ResourceFileComparatorSpec extends Specification {


	void "Sort files in the correct order"() {

		given:
		def files = [
			new File('zen.a.v1.json'),
			new File('zen.v1.json'),
			new File('zan.v1.json'),
			new File('zen.b.v1.json'),
			new File('root.v1.json')
		]

		and:
		Collections.sort(files, new ResourceFileComparator())

		and:
		def names = files.collect { f -> f.name }

		expect:
		names == [
			'root.v1.json',
			'zan.v1.json',
			'zen.v1.json',
			'zen.a.v1.json',
			'zen.b.v1.json'
		]
	}

	void "Sort files in the correct order 2"() {

		given:
		def files = [
			new File('root.v1.json'),
			new File('land.ort.v1.json'),
			new File('land.v1.json')
		]

		and:
		Collections.sort(files, new ResourceFileComparator())

		and:
		def names = files.collect { f -> f.name }

		expect:
		names == [
			'root.v1.json',
			'land.v1.json',
			'land.ort.v1.json'
		]
	}
}
