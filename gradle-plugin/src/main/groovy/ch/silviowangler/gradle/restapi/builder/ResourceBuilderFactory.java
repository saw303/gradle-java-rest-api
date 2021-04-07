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
package ch.silviowangler.gradle.restapi.builder;

import static ch.silviowangler.gradle.restapi.TargetFramework.SPRING_BOOT;

import ch.silviowangler.gradle.restapi.RestApiExtension;
import ch.silviowangler.gradle.restapi.TargetFramework;
import ch.silviowangler.gradle.restapi.builder.jaxrs.JaxRsRootResourceFactory;
import ch.silviowangler.gradle.restapi.builder.micronaut.MicronautResourceFactory;
import ch.silviowangler.gradle.restapi.builder.spring.SpringRootResourceFactory;
import java.util.Objects;

class ResourceBuilderFactory {

  public static ResourceBuilder getRootResourceBuilder(RestApiExtension restApiExtension) {

    TargetFramework targetFramework =
        Objects.requireNonNull(restApiExtension, "restApiExtension must not be null")
            .getTargetFramework();
    if (SPRING_BOOT == targetFramework) {
      return new SpringRootResourceFactory();
    } else if (targetFramework.isMicronaut()) {
      return new MicronautResourceFactory(restApiExtension);
    } else {
      return new JaxRsRootResourceFactory();
    }
  }
}
