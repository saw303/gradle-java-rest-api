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
package ch.silviowangler.rest.micronaut.binding;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.format.FormattingTypeConverter;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import javax.inject.Singleton;

/**
 * Converts an ISO 8601 UTC {@link CharSequence} such as 2007-12-24T18:21:00Z to a {@link Instant}.
 *
 * <p>Further information about ISO 8601 can be found <a
 * href="https://en.wikipedia.org/wiki/ISO_8601" target="_blank">here</a>.
 *
 * @author Silvio Wangler
 */
@Singleton
public class ISO8601DateTimeConverter
    implements FormattingTypeConverter<CharSequence, Instant, DateTimeFormat> {

  @Override
  public Class<DateTimeFormat> annotationType() {
    return DateTimeFormat.class;
  }

  @Override
  public Optional<Instant> convert(
      CharSequence object, Class<Instant> targetType, ConversionContext context) {
    try {
      return Optional.of(Instant.parse(object));
    } catch (DateTimeParseException ex) {
      return Optional.empty();
    }
  }
}
