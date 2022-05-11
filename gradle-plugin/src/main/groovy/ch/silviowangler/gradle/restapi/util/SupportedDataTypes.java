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
package ch.silviowangler.gradle.restapi.util;

import com.squareup.javapoet.ClassName;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

/**
 * @author Silvio Wangler
 */
public enum SupportedDataTypes {
  DATE(ClassName.get(LocalDate.class)),
  DATETIME(ClassName.get(Instant.class)),
  DECIMAL(ClassName.get(BigDecimal.class)),
  INT(ClassName.get(Integer.class)),
  LONG(ClassName.get(Long.class)),
  DOUBLE(ClassName.get(Double.class)),
  FLOAT(ClassName.get(Double.class)),
  BOOL(ClassName.get(Boolean.class)),
  FLAG(ClassName.get(Boolean.class)),
  STRING(ClassName.get(String.class)),
  UUID(ClassName.get(String.class)),
  OBJECT(ClassName.get(Object.class)),
  LOCALE(ClassName.get(Locale.class)),
  PHONE_NUMBER(ClassName.get(String.class)),
  JAVA_TIME_DURATION(ClassName.get(Duration.class)),
  MONEY(ClassName.get("javax.money", "MonetaryAmount"));

  private final ClassName className;

  SupportedDataTypes(ClassName className) {
    this.className = className;
  }

  public ClassName getClassName() {
    return className;
  }
}
