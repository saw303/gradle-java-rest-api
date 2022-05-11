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

/**
 * Inspired by the Spring Data's {@code Page} and GORM's {@code PagedResultList}, this models a type
 * that supports pagination operations.
 *
 * <p>A Page is a result set associated with a particular {@link Pageable} that includes a
 * calculation of the total size of number of records.
 *
 * @author Silvio Wangler
 * @since 2.2.0
 */
public interface Page<T> extends Slice<T> {

  /**
   * @return The total size of the all records.
   */
  long getTotalSize();

  /**
   * @return The total number of pages.
   */
  default int getTotalPages() {
    int size = getSize();
    return size == 0 ? 1 : (int) Math.ceil((double) getTotalSize() / (double) size);
  }

  /**
   * Indicated whether this is the last page or not.
   *
   * @return yes / no.
   */
  default boolean isLastPage() {
    return getTotalPages() - 1 == getPageNumber();
  }
}
