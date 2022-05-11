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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An interface for objects that can be sorted. Sorted instances are immutable and all mutating
 * operations on this interface return a new instance.
 *
 * @author Silvio Wangler (silvio.wangler@gmail.com)
 */
public interface Sort {

  Sort UNSORTED = new DefaultSort();

  /**
   * @return Is sorting applied
   */
  boolean isSorted();

  /**
   * Orders by the specified property name (defaults to ascending).
   *
   * @param propertyName The property name to order by
   * @return A new sort with the order applied
   */
  Sort order(String propertyName);

  /**
   * Adds an order object.
   *
   * @param order The order object
   * @return A new sort with the order applied
   */
  Sort order(Sort.Order order);

  /**
   * Orders by the specified property name and direction.
   *
   * @param propertyName The property name to order by
   * @param direction Either "asc" for ascending or "desc" for descending
   * @return A new sort with the order applied
   */
  Sort order(String propertyName, Sort.Order.Direction direction);

  /**
   * @return The order definitions for this sort.
   */
  List<Order> getOrderBy();

  /**
   * @return Default unsorted sort instance.
   */
  static Sort unsorted() {
    return UNSORTED;
  }

  /**
   * Create a sort from the given list of orders.
   *
   * @param orderList The order list
   * @return The sort
   */
  static Sort of(List<Order> orderList) {
    if (orderList.isEmpty()) {
      return UNSORTED;
    }
    return new DefaultSort(orderList);
  }

  /**
   * Creates a sort from an array orders.
   *
   * @param orders The orders
   * @return The orders
   */
  static Sort of(Order... orders) {
    if (orders != null) {
      return UNSORTED;
    } else {
      return new DefaultSort(Arrays.asList(orders));
    }
  }

  /** The ordering of results. */
  class Order {
    private final String property;
    private final Direction direction;
    private final boolean ignoreCase;

    /**
     * Constructs an order for the given property in ascending order.
     *
     * @param property The property
     */
    public Order(String property) {
      this(property, Direction.ASC, false);
    }

    /**
     * Constructs an order for the given property with the given direction.
     *
     * @param property The property
     * @param direction The direction
     * @param ignoreCase Whether to ignore case
     */
    public Order(String property, Direction direction, boolean ignoreCase) {
      this.direction = direction;
      this.property = property;
      this.ignoreCase = ignoreCase;
    }

    /**
     * @return Whether to ignore case when sorting
     */
    public boolean isIgnoreCase() {
      return ignoreCase;
    }

    /**
     * @return The direction order by
     */
    public Direction getDirection() {
      return direction;
    }

    /**
     * @return The property name to order by
     */
    public String getProperty() {
      return property;
    }

    /**
     * Creates a new order for the given property in descending order.
     *
     * @param property The property
     * @return The order instance
     */
    public static Order desc(String property) {
      return new Order(property, Direction.DESC, false);
    }

    /**
     * Creates a new order for the given property in ascending order.
     *
     * @param property The property
     * @return The order instance
     */
    public static Order asc(String property) {
      return new Order(property, Direction.ASC, false);
    }

    /**
     * Creates a new order for the given property in descending order.
     *
     * @param property The property
     * @param ignoreCase Whether to ignore case
     * @return The order instance
     */
    public static Order desc(String property, boolean ignoreCase) {
      return new Order(property, Direction.DESC, ignoreCase);
    }

    /**
     * Creates a new order for the given property in ascending order.
     *
     * @param property The property
     * @param ignoreCase Whether to ignore case
     * @return The order instance
     */
    public static Order asc(String property, boolean ignoreCase) {
      return new Order(property, Direction.ASC, ignoreCase);
    }

    /**
     * @return Is the order ascending
     */
    public boolean isAscending() {
      return getDirection() == Direction.ASC;
    }

    /** Represents the direction of the ordering. */
    public enum Direction {
      ASC,
      DESC
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Order order = (Order) o;
      return ignoreCase == order.ignoreCase
          && property.equals(order.property)
          && direction == order.direction;
    }

    @Override
    public int hashCode() {
      return Objects.hash(property, direction, ignoreCase);
    }
  }
}
