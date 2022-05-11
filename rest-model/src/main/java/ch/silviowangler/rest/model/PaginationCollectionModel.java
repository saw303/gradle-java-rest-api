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
package ch.silviowangler.rest.model;

import ch.silviowangler.rest.model.pagination.Page;
import ch.silviowangler.rest.model.pagination.Slice;

/**
 * @author Silvio Wangler
 */
public class PaginationCollectionModel<R extends ResourceModel> extends CollectionModel<R> {

  private PaginationModel pagination;

  public PaginationCollectionModel() {}

  public PaginationCollectionModel(Slice slice) {

    this.pagination = new PaginationModel(slice);
  }

  public PaginationCollectionModel(Page page) {
    this.pagination = new PaginationModel(page);
  }

  public PaginationModel getPagination() {
    return pagination;
  }

  public void setPagination(PaginationModel pagination) {
    this.pagination = pagination;
  }
}
