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
package ch.silviowangler.rest.spring;

import ch.silviowangler.rest.NotYetImplementedException;
import ch.silviowangler.rest.model.FieldError;
import ch.silviowangler.rest.model.ValidationErrorModel;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Silvio Wangler
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {NotYetImplementedException.class})
  protected ResponseEntity<Object> handleNotYetImplemented(
      RuntimeException ex, WebRequest request) {

    return handleExceptionInternal(
        ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED, request);
  }

  /**
   * Converts {@link MethodArgumentNotValidException} into {@link ValidationErrorModel}.
   *
   * @param ex the exception
   * @param headers headers
   * @param status http status
   * @param request the http request
   * @return a validation model according the failed validation constraints.
   * @since 1.0.22
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {

    List<org.springframework.validation.FieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors();
    List<FieldError> errors = new ArrayList<>(fieldErrors.size());

    for (org.springframework.validation.FieldError error : fieldErrors) {
      errors.add(new FieldError(error.getField(), error.getRejectedValue()));
    }

    ValidationErrorModel errorModel = new ValidationErrorModel(errors);
    return new ResponseEntity<>(errorModel, headers, status);
  }
}
