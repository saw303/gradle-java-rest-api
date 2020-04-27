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
import ch.silviowangler.rest.contract.model.v1.Verb;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * You may only have POST_ENTITY or POST_COLLECTION verbs but not both in the same specification.
 *
 * <p>Each line below declares a valid combination. They must not be combined.
 *
 * <ol>
 *   <li>POST
 *   <li>POST_ENTITY
 *   <li>POST_COLLECTION
 * </ol>
 *
 * @author Silvio Wangler (silvio.wangler@gmail.com)
 * @since 2.1.1
 */
public class OnlyOnePostVerbValidator implements Validator {
  @Override
  public Set<ConstraintViolation> validate(ResourceContract resourceContract) {

    Set<ConstraintViolation> violations = new HashSet<>();

    Set<String> candidates =
        resourceContract.getVerbs().stream()
            .map(Verb::getVerb)
            .filter(v -> ResourceBuilder.POST_METHODS.contains(v))
            .collect(Collectors.toSet());

    if (candidates.size() > 1) {
      violations.add(
          new ConstraintViolation(
              String.format(
                  "Choose either POST, POST_ENTITY or POST_COLLECTION. Only one of them is allowed since they map all to the same URI path. You chose %s",
                  candidates.stream().collect(Collectors.joining(", "))),
              this,
              resourceContract));
    }

    return violations;
  }
}
