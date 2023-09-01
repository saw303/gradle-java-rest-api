/*
 * MIT License
 * <p>
 * Copyright (c) 2016 - 2023 Silvio Wangler (silvio.wangler@gmail.com)
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
package ch.silviowangler.rest.serdes;

import ch.silviowangler.rest.model.ResourceModel;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.type.Argument;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import jakarta.inject.Singleton;
import java.io.IOException;

@Singleton
@Secondary
public class ResourceModelDeserializer implements Deserializer<ResourceModel> {
  @Override
  public @Nullable ResourceModel deserialize(
      @NonNull Decoder decoder,
      @NonNull DecoderContext context,
      @NonNull Argument<? super ResourceModel> type)
      throws IOException {

    JsonNode jsonNode = decoder.decodeNode();
    JsonNode classValue = jsonNode.get(JsonTypeInfo.Id.CLASS.getDefaultPropertyName());

    try {
      Class<ResourceModel> clazz =
          (Class<ResourceModel>) Class.forName(classValue.getStringValue());
      BeanIntrospection<ResourceModel> introspection = BeanIntrospection.getIntrospection(clazz);

      ResourceModel resourceModel = introspection.instantiate();
      introspection
          .getBeanProperties()
          .forEach(
              beanProperty ->
                  beanProperty.set(
                      resourceModel,
                      context
                          .getConversionService()
                          .convert(
                              jsonNode.get(beanProperty.getName()).getValue(),
                              beanProperty.getType())
                          .orElseThrow()));

      return resourceModel;

    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
