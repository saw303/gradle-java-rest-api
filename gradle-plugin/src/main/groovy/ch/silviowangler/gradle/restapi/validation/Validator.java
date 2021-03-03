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

import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import ch.silviowangler.rest.contract.model.v1.ResourceTypes;
import java.util.List;
import java.util.Set;

/**
 * Validator interface to for resource contract validators.
 *
 * @author Silvio Wangler
 * @since 2.1.0
 */
public interface Validator {

  /**
   * Validates the given resource contract and reports potential constraint validation errors in
   * return. An empty result means that the validation was successful and is good.
   *
   * @param resourceContract the resource contract to validate.
   * @return constraint validations, if empty the validation did not found any constraint
   *     violations.
   */
  Set<ConstraintViolation> validate(ResourceContract resourceContract);

  /**
   * Validates the given resource contract and reports potential constraint validation errors in
   * return. An empty result means that the validation was successful and is good.
   *
   * @param resourceContract the resource contract to validate.
   * @param definedResourceTypes the resource types which have already been defined in resources
   *     further up the hierarchy.
   * @return constraint validations, if empty the validation did not found any constraint
   *     violations.
   */
  default Set<ConstraintViolation> validate(
      ResourceContract resourceContract, List<ResourceTypes> definedResourceTypes) {
    return validate(resourceContract);
  }
}
