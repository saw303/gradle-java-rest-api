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
package ch.silviowangler.rest.model.pagination;

import java.util.Iterator;
import java.util.List;

/**
 * Inspired by the Micronaut Data's {@code Slice} and GORM's {@code PagedResultList}, this models a
 * type that supports pagination operations.
 *
 * <p>A slice is a result list associated with a particular {@link Pageable}
 *
 * @author Silvio Wangler
 * @since 2.2.0
 */
public interface Slice<T> extends Iterable<T> {

  /**
   * @return The content.
   */
  List<T> getContent();

  /**
   * @return The pageable for this slice.
   */
  Pageable getPageable();

  /**
   * @return The page number
   */
  default int getPageNumber() {
    return getPageable().getNumber();
  }
  /**
   * @return The offset.
   */
  default long getOffset() {
    return getPageable().getOffset();
  }

  /**
   * @return The size of the slice.
   */
  default int getSize() {
    return getPageable().getSize();
  }

  /**
   * @return Whether the slize is empty
   */
  default boolean isEmpty() {
    return getContent().isEmpty();
  }

  /**
   * @return The number of elements
   */
  default int getNumberOfElements() {
    return getContent().size();
  }

  @Override
  default Iterator<T> iterator() {
    return getContent().iterator();
  }
}
