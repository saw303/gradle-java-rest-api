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
package ch.silviowangler.rest.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primitives Filtermodell, welches die den Queryparameter "filter" auflöst.
 *
 * <p>Derzeit sind OR-Filter nicht erlaubt, da diese auch nicht in einer sinnvollen Art und Weise
 * durch SYRIUS behandelt werden können.
 *
 * @author Marco Hofstetter
 * @author Silvio Wangler
 */
public class FilterModel {

  private final Map<String, FilterCondition> filters;
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterModel.class);

  public FilterModel() {
    filters = new HashMap<>();
  }

  public FilterModel(String filterString) {
    this.filters = parseFilter(filterString);
  }

  protected Map<String, FilterCondition> parseFilter(String filterString) {

    LOGGER.debug("Parsing JSON String '{}'", filterString);

    Map<String, FilterCondition> filter = new HashMap<>();

    JsonArray jsonArray = new Gson().fromJson(filterString, JsonArray.class);

    if (jsonArray == null) {
      throw new IllegalArgumentException();
    }

    final int elementCount = jsonArray.size();
    LOGGER.debug("Element count is {}", elementCount);

    if (elementCount == 0) {
      return filter;
    }

    detectUnsupportedOrOperations(jsonArray, elementCount);

    addFilter(filter, jsonArray);
    return filter;
  }

  public Collection<String> filteredFields() {
    return filters.keySet();
  }

  public FilterCondition get(String field) {
    return filters.get(field);
  }

  public static class FilterCondition {
    private final FilterOperation op;
    private final String value;

    public FilterCondition(FilterOperation op, String value) {
      this.op = op;
      this.value = value;
    }

    public FilterOperation getOp() {
      return op;
    }

    public String getValueAsString() {
      return value.replaceAll("\"", "");
    }

    public List<String> getValueAsStringList() {

      if ("[]".equals(value)) {
        return Collections.emptyList();
      }

      if (value.startsWith("[") && value.endsWith("]")) {
        String values = value.replace("[", "").replace("]", "");
        return new ArrayList<>(Arrays.asList(values.split(",")));
      }

      throw new IllegalStateException("Value ist kein Array");
    }

    public Integer getValueAsInteger() {
      return Integer.valueOf(value);
    }

    public List<Integer> getValueAsIntegerList() {
      return getValueAsStringList().stream().map(Integer::valueOf).collect(Collectors.toList());
    }
  }

  public enum FilterOperation {
    lt,
    lte,
    gt,
    gte,
    eq,

    in,
    btw,
    sw,
    ew,
    con,

    neq,
    nbtw,
    notnull,
    ncon,
    nsw,
    NEW,
    nin
  }

  private void detectUnsupportedOrOperations(JsonArray jsonArray, int elementCount) {
    final JsonElement element1 = jsonArray.get(0);
    if (elementCount == 1) {
      // muss ein array sein
      if (!element1.isJsonArray()) {
        throw new IllegalArgumentException("Muss ein JSON Array sein");
      }
    } else if (elementCount == 3) {
      // die ersten beiden elemente sind primitive JSON Objekte
      final JsonElement element2 = jsonArray.get(1);
      final JsonElement element3 = jsonArray.get(2);

      boolean valid = element1.isJsonPrimitive() && element2.isJsonPrimitive();

      valid = valid && element3.isJsonPrimitive() || element3.isJsonArray();

      if (!valid) {
        throw new IllegalArgumentException();
      }
    } else {
      throw new UnsupportedOperationException("OR Operationen werden nicht unterstützt");
    }
  }

  private void addFilter(Map<String, FilterCondition> filter, JsonArray jsonArray) {

    final int size = jsonArray.size();

    if (size == 1) {
      addFilter(filter, jsonArray.get(0).getAsJsonArray());
      return;
    }

    assert size >= 3 : "Group count must be at least 3. Current is " + size;

    String fieldName = jsonArray.get(0).getAsString();
    String operator = jsonArray.get(1).getAsString();
    JsonElement jsonElement = jsonArray.get(2);

    String value;
    if (jsonElement instanceof JsonPrimitive) {
      value = jsonElement.getAsString();
    } else {
      value = jsonArrayToString((JsonArray) jsonElement);
    }

    filter.put(fieldName, new FilterCondition(FilterOperation.valueOf(operator), value));

    if (size == 4) {
      addFilter(filter, jsonArray.get(3).getAsJsonArray());
    }
  }

  private String jsonArrayToString(JsonArray jsonElement) {
    String value;
    JsonArray valueArray = jsonElement;
    Iterator<JsonElement> iterator = valueArray.iterator();

    StringBuilder sb = new StringBuilder("[");

    for (int i = 0; iterator.hasNext(); i++) {
      sb.append(iterator.next().getAsString());
      if (i < valueArray.size() - 1) {
        sb.append(",");
      }
    }
    sb.append("]");
    value = sb.toString();
    return value;
  }
}
