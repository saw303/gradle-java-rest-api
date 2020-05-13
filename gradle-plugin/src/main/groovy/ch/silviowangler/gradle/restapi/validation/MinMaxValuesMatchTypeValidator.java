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
package ch.silviowangler.gradle.restapi.validation;

import ch.silviowangler.gradle.restapi.builder.ResourceBuilder;
import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.ResourceField;
import com.squareup.javapoet.ClassName;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Silvio Wangler
 * @since 2.1.0
 */
public class MinMaxValuesMatchTypeValidator implements Validator {

  private final List<String> unsupportedTypes =
      Arrays.asList("bool", "date", "datetime", "flag", "uuid", "object", "locale");

  private final Predicate<ResourceField> minIsPresent = field -> field.getMin() != null;
  private final Predicate<ResourceField> maxIsPresent = field -> field.getMax() != null;

  @Override
  public Set<ConstraintViolation> validate(ResourceContract resourceContract) {

    Set<ConstraintViolation> violations = new HashSet<>();

    List<ResourceField> fieldsWithMinAndOrMaxConstraints =
        resourceContract.getFields().stream()
            .filter(minIsPresent.or(maxIsPresent))
            .collect(Collectors.toList());

    for (ResourceField field : fieldsWithMinAndOrMaxConstraints) {

      // does the current datatype support min/max constraints?
      if (unsupportedTypes.contains(field.getType())) {
        violations.add(
            new ConstraintViolation(
                String.format(
                    "Min/Max not supported for field '%s' with type '%s'",
                    field.getName(), field.getType()),
                this,
                resourceContract));
        continue;
      }

      boolean isString = Objects.equals("string", field.getType());

      ClassName className = ResourceBuilder.JavaTypeRegistry.translateToJava(field);
      String fieldClassName = className.reflectionName();

      if (field.getMin() != null) {

        String minClassName = field.getMin().getClass().getCanonicalName();

        if (isString) {
          if (!Number.class.isAssignableFrom(field.getMin().getClass())) {
            violations.add(
                new ConstraintViolation(
                    String.format(
                        "Min constraints of field '%s' must be of type 'Number' but is '%s'",
                        field.getName(), minClassName),
                    this,
                    resourceContract));
          }
        } else if (!Objects.equals(minClassName, fieldClassName)) {
          violations.add(
              new ConstraintViolation(
                  String.format(
                      "Min constraints of field '%s' must be of type '%s' but is '%s'",
                      field.getName(), fieldClassName, minClassName),
                  this,
                  resourceContract));
        }
      }

      if (field.getMax() != null) {

        String maxClassName = field.getMax().getClass().getCanonicalName();

        if (isString) {
          if (!Number.class.isAssignableFrom(field.getMax().getClass())) {
            violations.add(
                new ConstraintViolation(
                    String.format(
                        "Min constraints of field '%s' must be of type 'Number' but is '%s'",
                        field.getName(), maxClassName),
                    this,
                    resourceContract));
          }
        } else if (!Objects.equals(maxClassName, fieldClassName)) {
          violations.add(
              new ConstraintViolation(
                  String.format(
                      "Max constraints of field '%s' must be of type '%s' but is '%s'",
                      field.getName(), fieldClassName, maxClassName),
                  this,
                  resourceContract));
        }
      }
    }
    return violations;
  }
}
