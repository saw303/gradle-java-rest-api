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
package ch.silviowangler.rest.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates international phone numbers using Google's phone library {@link PhoneNumberUtil}.
 *
 * <p>The validator will respond with a positive answer when validating a null value. If you want to
 * enforce non nullables values you need add the constraint {@link
 * jakarta.validation.constraints.NotNull}.
 *
 * @author Silvio Wangler
 * @since 2.0.11
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

  private String country;

  @Override
  public void initialize(PhoneNumber constraintAnnotation) {
    this.country = constraintAnnotation.country();
  }

  @Override
  public boolean isValid(String phoneNumberAsString, ConstraintValidatorContext context) {

    // if you want to enforce non nullable value use @NotNull
    if (phoneNumberAsString == null) return true;

    try {
      Phonenumber.PhoneNumber phoneNumber =
          PhoneNumberUtil.getInstance().parse(phoneNumberAsString, this.country);
      return PhoneNumberUtil.getInstance().isValidNumber(phoneNumber);
    } catch (NumberParseException e) {
      return false;
    }
  }
}
