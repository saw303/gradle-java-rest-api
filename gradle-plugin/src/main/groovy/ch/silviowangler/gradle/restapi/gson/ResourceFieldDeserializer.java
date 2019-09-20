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
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/** @author Silvio Wangler */
public class ResourceFieldDeserializer implements JsonDeserializer<ResourceField> {

  @Override
  public ResourceField deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    ResourceField field = new ResourceField();

    JsonObject jsonObject = json.getAsJsonObject();

    field.setName(asString(jsonObject.get("name")));

    String type = asString(jsonObject.get("type"));
    field.setType(type);

    if (Objects.equals("enum", type)) {
      field.setOptions(asList(jsonObject.getAsJsonArray("options")));
    } else {
      field.setOptions(asString(jsonObject.get("options")));
    }

    Iterator<JsonElement> iterator = jsonObject.get("mandatory").getAsJsonArray().iterator();
    List<String> mandatoryValues = new ArrayList<>();
    while (iterator.hasNext()) {
      mandatoryValues.add(asString(iterator.next()));
    }
    field.setMandatory(mandatoryValues);

    field.setMin(asNumberForType(jsonObject.get("min"), type));
    field.setMax(asNumberForType(jsonObject.get("max"), type));
    field.setMultiple(asBoolean(jsonObject.get("multiple")));
    field.setDefaultValue(asString(jsonObject.get("defaultValue")));
    field.setShield(null);
    field.setVisible(asBoolean(jsonObject.get("visible")));
    field.setSortable(asBoolean(jsonObject.get("sortable")));
    field.setReadonly(asBoolean(jsonObject.get("readonly")));
    field.setFilterable(asBoolean(jsonObject.get("filterable")));
    field.setxComment(asString(jsonObject.get("x-comment")));

    return field;
  }

  private List<String> asList(JsonArray jsonArray) {

    List<String> list = new ArrayList<>(jsonArray.size());

    for (JsonElement jsonElement : jsonArray) {
      list.add(asString(jsonElement));
    }

    return list;
  }

  private String asString(JsonElement jsonElement) {

    if (jsonElement == null) return null;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsString();
    } else {
      return null;
    }
  }

  private Number asNumberForType(JsonElement jsonElement, String type) {

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
          throw new RuntimeException("Unknown number type " + type);
      }
    } else {
      return null;
    }
  }

  private boolean asBoolean(JsonElement jsonElement) {
    if (jsonElement == null) return false;

    if (!jsonElement.isJsonNull()) {
      return jsonElement.getAsBoolean();
    } else {
      return false;
    }
  }
}
