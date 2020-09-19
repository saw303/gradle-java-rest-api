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

import ch.silviowangler.rest.contract.model.v1.Representation;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Optional;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class RepresentationDeserializer implements JsonDeserializer<Representation> {
  @Override
  public Representation deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Representation representation = new Representation();

    JsonObject jsonObject = json.getAsJsonObject();
    representation.setName(jsonObject.get("name").getAsString());
    representation.setComment(jsonObject.get("comment").getAsString());
    representation.setResponseExample(jsonObject.get("responseExample").getAsString());
    representation.setStandard(readBool(jsonObject.get("isDefault")).orElse(false));
    representation.setRaw(readBool(jsonObject.get("raw")).orElse(false));
    String mimeType = jsonObject.get("mimetype").getAsString();

    try {

      if ("json".equals(representation.getName())) {
        representation.setMimetype(new MimeType("application", "json"));
      } else {
        representation.setMimetype(new MimeType(mimeType));
      }

    } catch (MimeTypeParseException e) {
      throw new JsonParseException("Cannot parse mime type " + mimeType, e);
    }

    return representation;
  }

  private Optional<Boolean> readBool(JsonElement jsonElement) {

    if (jsonElement != null) {
      return Optional.of(jsonElement.getAsBoolean());
    }
    return Optional.empty();
  }
}
