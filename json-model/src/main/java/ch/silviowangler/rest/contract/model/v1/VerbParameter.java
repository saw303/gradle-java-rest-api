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
package ch.silviowangler.rest.contract.model.v1;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Silvio Wangler
 */
public class VerbParameter implements Serializable, FieldType {

  private String name;
  private String type;
  private Object options;
  private boolean mandatory;
  private Number min;
  private Number max;
  private boolean multiple;
  private Object defaultValue;
  // in JSON protected
  private String shield;
  private boolean visible;
  private boolean sortable;
  private boolean readonly;
  private boolean filterable;
  private List<String> alias;
  private String xComment;

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public Object getOptions() {
    return options;
  }

  public void setOptions(Object options) {
    this.options = options;
  }

  public boolean getMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  public Number getMin() {
    return min;
  }

  public void setMin(Number min) {
    this.min = min;
  }

  public Number getMax() {
    return max;
  }

  public void setMax(Number max) {
    this.max = max;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public void setMultiple(boolean multiple) {
    this.multiple = multiple;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getShield() {
    return shield;
  }

  public void setShield(String shield) {
    this.shield = shield;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isSortable() {
    return sortable;
  }

  public void setSortable(boolean sortable) {
    this.sortable = sortable;
  }

  public boolean isReadonly() {
    return readonly;
  }

  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  public boolean isFilterable() {
    return filterable;
  }

  public void setFilterable(boolean filterable) {
    this.filterable = filterable;
  }

  public List<String> getAlias() {
    return alias;
  }

  public void setAlias(List<String> alias) {
    this.alias = alias;
  }

  public String getxComment() {
    return xComment;
  }

  public void setxComment(String xComment) {
    this.xComment = xComment;
  }

  public boolean hasMinMaxConstraints() {
    return getMin() != null || getMax() != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VerbParameter that = (VerbParameter) o;
    return mandatory == that.mandatory
        && multiple == that.multiple
        && visible == that.visible
        && sortable == that.sortable
        && readonly == that.readonly
        && filterable == that.filterable
        && Objects.equals(name, that.name)
        && Objects.equals(type, that.type)
        && Objects.equals(options, that.options)
        && Objects.equals(min, that.min)
        && Objects.equals(max, that.max)
        && Objects.equals(defaultValue, that.defaultValue)
        && Objects.equals(shield, that.shield)
        && Objects.equals(alias, that.alias)
        && Objects.equals(xComment, that.xComment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        type,
        options,
        mandatory,
        min,
        max,
        multiple,
        defaultValue,
        shield,
        visible,
        sortable,
        readonly,
        filterable,
        alias,
        xComment);
  }
}
