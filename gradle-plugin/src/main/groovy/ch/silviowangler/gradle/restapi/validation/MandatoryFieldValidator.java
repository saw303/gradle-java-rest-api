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
import java.util.HashSet;
import java.util.Set;

/**
 * Verifies that mandatory fields are mandatory on verbs the resource obtains.
 *
 * @author Silvio Wangler
 */
public class MandatoryFieldValidator implements Validator {
  @Override
  public Set<ConstraintViolation> validate(ResourceContract resourceContract) {

    Set<ConstraintViolation> violations = new HashSet<>();

    resourceContract.getFields().stream()
        .filter(f -> !f.getMandatory().isEmpty())
        .forEach(
            f -> {
              for (String verb : f.getMandatory()) {

                if (resourceContract.getVerbs().stream().noneMatch(v -> v.getVerb().equals(verb))) {
                  violations.add(
                      new ConstraintViolation(
                          String.format(
                              "Field '%s' cannot be mandatory on non existing verb '%s'",
                              f.getName(), verb),
                          this,
                          resourceContract));
                }
              }
            });

    return violations;
  }
}
