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
package ch.silviowangler.rest.micronaut

import spock.lang.Specification

import static ch.silviowangler.rest.micronaut.ExpandedGetResponseFilter.UriPlaceholderReplacer.replacePlaceholders

/**
 * @author Silvio Wangler
 */
class UriPlaceholderReplacerSpec extends Specification {

    void "replace one placeholder placeholder"() {

        when:
        String result = replacePlaceholders('/v1/countries/{:entity}/cities/', ['id': 'CHE'])

        then:
        result == '/v1/countries/CHE/cities/'
    }

    void "replace several placeholders placeholder"() {

        when:
        String result = replacePlaceholders('/v1/countries/{:country}/cities/{:entity}/municipalities', ['id': 'ZH', 'country': 'CHE'])

        then:
        result == '/v1/countries/CHE/cities/ZH/municipalities'

    }

}
