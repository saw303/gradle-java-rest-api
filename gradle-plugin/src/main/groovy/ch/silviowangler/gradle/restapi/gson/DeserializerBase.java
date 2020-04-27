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
package ch.silviowangler.gradle.restapi.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for Jackson deserializers.
 *
 * @author Silvio Wangler (silvio.wangler@gmail.ch)
 */
public abstract class DeserializerBase {

  protected final List<String> asList(JsonArray jsonArray) {

    List<String> list = new ArrayList<>(jsonArray.size());

    for (JsonElement jsonElement : jsonArray) {
      list.add(asString(jsonElement));
    }

    return list;
  }

  protected final String asString(JsonElement jsonElement) {

    if (jsonElement == null) return null;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsString();
    } else {
      return null;
    }
  }

  protected final Number asNumber(JsonElement jsonElement) {

    if (jsonElement == null) return null;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsNumber();
    } else {
      return null;
    }
  }

  protected final boolean asBoolean(JsonElement jsonElement) {
    if (jsonElement == null) return false;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsBoolean();
    } else {
      return false;
    }
  }

  protected Number asNumberForType(JsonElement jsonElement, String type) {

    if (jsonElement == null) return null;

    if (!jsonElement.isJsonNull()) {

      switch (type) {
        case "string":
        case "int":
          return jsonElement.getAsInt();
        case "double":
        case "float":
        case "decimal":
        case "money":
          return jsonElement.getAsDouble();
        case "long":
          return jsonElement.getAsLong();
        default:
          return null;
      }
    } else {
      return null;
    }
  }
}
