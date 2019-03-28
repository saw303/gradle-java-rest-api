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
package ch.silviowangler.rest.micronaut.binding

import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * @author Silvio Wangler
 */
class ISO8601DateTimeConverterSpec extends Specification {

	@Subject
	ISO8601DateTimeConverter converter = new ISO8601DateTimeConverter()

	@Unroll
	void "Convert timestamp #timestamp to instant"() {

		given:
		Optional<Instant> potentialInstant = converter.convert(timestamp, Instant.class)

		expect:
		potentialInstant.isPresent()

		and:
		potentialInstant.get() == instant

		where:
		timestamp                   || instant
		'1900-01-01T14:00:00Z'      || LocalDateTime.of(1900, 1, 1, 14, 0, 0).toInstant(ZoneOffset.UTC)
		'2007-08-31T16:47:00Z'      || LocalDateTime.of(2007, 8, 31, 16, 47, 0).toInstant(ZoneOffset.UTC)
	}

	@Unroll
	@IgnoreIf({ javaVersion < 12 })
	void "Convert timestamp #timestamp to instant (Java 9)"() {

		given:
		Optional<Instant> potentialInstant = converter.convert(timestamp, Instant.class)

		expect:
		potentialInstant.isPresent()

		and:
		potentialInstant.get() == instant

		where:
		timestamp                   || instant
		'2007-08-31T16:47:00+00:00' || LocalDateTime.of(2007, 8, 31, 16, 47, 0).toInstant(ZoneOffset.UTC)
	}

	@Unroll
	@IgnoreIf({ javaVersion >= 12 })
	void "Do not convert timestamp #timestamp to instant since it is not UTC conform (Pre Java 12)"() {

		given:
		Optional<Instant> potentialInstant = converter.convert(timestamp, Instant.class)

		expect:
		!potentialInstant.isPresent()

		where:
		timestamp                   | _
		'2007-08-31T16:47:00+00:00' | _
		'2007-08-31T16:47+00:00'    | _
		'2007-08-31T16:47:00+01:00' | _
		'2007-08-31T16:47:00+09:00' | _
	}
}
