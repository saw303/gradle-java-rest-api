/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2019 Silvio Wangler (silvio.wangler@gmail.com)
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

import ch.silviowangler.rest.contract.model.v1.ResourceField;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** @author Silvio Wangler */
public class ResourceFieldDeserializer implements JsonDeserializer<ResourceField> {
  @Override
  public ResourceField deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    ResourceField field = new ResourceField();

    JsonObject jsonObject = json.getAsJsonObject();

    field.setName(toString(jsonObject.get("name")));
    field.setType(toString(jsonObject.get("type")));
    field.setOptions(toString(jsonObject.get("options")));

    Iterator<JsonElement> iterator = jsonObject.get("mandatory").getAsJsonArray().iterator();
    List<String> mandatoryValues = new ArrayList<>();
    while (iterator.hasNext()) {
      mandatoryValues.add(toString(iterator.next()));
    }
    field.setMandatory(mandatoryValues);

    field.setMin(toNumber(jsonObject.get("min")));
    field.setMax(toNumber(jsonObject.get("max")));
    field.setMultiple(toBoolean(jsonObject.get("multiple")));
    field.setDefaultValue(toString(jsonObject.get("defaultValue")));
    field.setShield(null);
    field.setVisible(toBoolean(jsonObject.get("visible")));
    field.setSortable(toBoolean(jsonObject.get("sortable")));
    field.setReadonly(toBoolean(jsonObject.get("readonly")));
    field.setFilterable(toBoolean(jsonObject.get("filterable")));
    field.setxComment(toString(jsonObject.get("x-comment")));

    return field;
  }

  String toString(JsonElement jsonElement) {

    if (jsonElement == null) return null;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsString();
    } else {
      return null;
    }
  }

  Number toNumber(JsonElement jsonElement) {

    if (jsonElement == null) return null;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsNumber();
    } else {
      return null;
    }
  }

  boolean toBoolean(JsonElement jsonElement) {
    if (jsonElement == null) return false;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsBoolean();
    } else {
      return false;
    }
  }
}
