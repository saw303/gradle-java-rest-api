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

import ch.silviowangler.rest.contract.model.v1.CustomTypeField;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author Silvio Wangler
 */
public class CustomTypeFieldDeserializer extends DeserializerBase
    implements JsonDeserializer<CustomTypeField> {

  @Override
  public CustomTypeField deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    CustomTypeField field = new CustomTypeField();

    JsonObject jsonObject = json.getAsJsonObject();

    field.setName(asString(jsonObject.get("name")));

    String type = asString(jsonObject.get("type"));
    field.setType(type);

    if (Objects.equals("enum", type)) {
      field.setOptions(asList(jsonObject.getAsJsonArray("options")));
    } else {
      field.setOptions(asString(jsonObject.get("options")));
    }

    field.setMin(asNumber(jsonObject.get("min")));
    field.setMax(asNumber(jsonObject.get("max")));
    field.setMultiple(asBoolean(jsonObject.get("multiple")));
    field.setDefaultValue(asString(jsonObject.get("defaultValue")));
    field.setComment(asString(jsonObject.get("x-comment")));

    return field;
  }
}
