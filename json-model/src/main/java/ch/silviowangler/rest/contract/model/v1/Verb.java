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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Silvio Wangler
 */
public class Verb implements Serializable {

  private String verb;
  private String rel;
  private List<ResponseState> responseStates = new ArrayList<>();
  private List<Representation> representations = new ArrayList<>();
  private List<VerbParameter> parameters = new ArrayList<>();
  private List<Header> headers = new ArrayList<>();

  public Verb() {}

  public Verb(String verb) {
    this.verb = verb;
  }

  public String getVerb() {
    return verb;
  }

  public void setVerb(String verb) {
    this.verb = verb;
  }

  public String getRel() {
    return rel;
  }

  public void setRel(String rel) {
    this.rel = rel;
  }

  public List<ResponseState> getResponseStates() {
    return responseStates;
  }

  public void setResponseStates(List<ResponseState> responseStates) {
    this.responseStates = responseStates;
  }

  public List<Representation> getRepresentations() {
    return representations;
  }

  public void setRepresentations(List<Representation> representations) {
    this.representations = representations;
  }

  public List<VerbParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<VerbParameter> parameters) {
    this.parameters = parameters;
  }

  public List<Header> getHeaders() {
    return headers;
  }

  public void setHeaders(List<Header> headers) {
    this.headers = headers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Verb verb1 = (Verb) o;
    return Objects.equals(verb, verb1.verb)
        && Objects.equals(rel, verb1.rel)
        && Objects.equals(responseStates, verb1.responseStates)
        && Objects.equals(representations, verb1.representations)
        && Objects.equals(parameters, verb1.parameters)
        && Objects.equals(headers, verb1.headers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(verb, rel, responseStates, representations, parameters, headers);
  }

  @Override
  public String toString() {
    return this.getVerb();
  }

  public boolean containsRepresentation(String name) {
    return this.getRepresentations().stream().anyMatch(r -> r.getName().equals(name));
  }

  public boolean containsRepresentationJson() {
    return containsRepresentation("json");
  }
}
