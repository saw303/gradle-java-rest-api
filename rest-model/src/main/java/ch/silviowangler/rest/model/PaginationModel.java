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
import java.io.Serializable;

/**
 * Context information for a client for pagination.
 *
 * @author Silvio Wangler (silvio.wangler@gmail.com)
 */
public class PaginationModel implements Serializable {

  private int size;
  private long totalSize;
  private int page;
  private int totalPages;

  public PaginationModel(Slice slice) {
    this.size = slice.getSize();
    this.page = slice.getPageNumber();
    this.totalPages = -1;
    this.totalSize = -1;
  }

  public PaginationModel(Page page) {
    this((Slice) page);
    this.totalPages = page.getTotalPages();
    this.totalSize = page.getTotalSize();
  }

  // Used for Jackson deserialization
  public PaginationModel() {
    this.size = 0;
    this.totalSize = 0L;
    this.page = 0;
    this.totalPages = 0;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public long getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(long totalSize) {
    this.totalSize = totalSize;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }
}
