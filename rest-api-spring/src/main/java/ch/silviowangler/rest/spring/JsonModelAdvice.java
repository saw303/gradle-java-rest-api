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

import ch.silviowangler.rest.model.CollectionModel;
import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.model.Identifiable;
import ch.silviowangler.rest.model.ResourceLink;
import ch.silviowangler.rest.model.ResourceModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Converts the a resource model to the general JSON structure that is used application wide.
 *
 * @author Silvio Wangler
 */
@ControllerAdvice
class JsonModelAdvice implements ResponseBodyAdvice {

  private static final Logger log = LoggerFactory.getLogger(JsonModelAdvice.class);

  @Override
  public boolean supports(MethodParameter returnType, Class converterType) {
    Class<?> parameterType = returnType.getParameterType();
    return ResourceModel.class.isAssignableFrom(parameterType)
        || Collection.class.isAssignableFrom(parameterType)
            && HttpMessageConverter.class.isAssignableFrom(converterType);
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    if (MediaType.APPLICATION_JSON.includes(selectedContentType)) {

      if (body instanceof ResourceModel) {
        EntityModel model = new EntityModel((ResourceModel) body);
        model.getLinks().add(ResourceLink.selfLink(request.getURI().getPath()));
        return model;
      } else if (body instanceof List) {

        List list = (List) body;
        CollectionModel collectionModel = new CollectionModel();
        collectionModel.getLinks().add(ResourceLink.selfLink(request.getURI().getPath()));

        List<EntityModel> entityModels = new ArrayList<>(list.size());

        for (Object l : list) {
          if (l instanceof ResourceModel) {
            ResourceModel resourceModel = (ResourceModel) l;
            EntityModel entityModel = new EntityModel(resourceModel);
            if (l instanceof Identifiable) {
              entityModel
                  .getLinks()
                  .add(
                      ResourceLink.selfLink(
                          request.getURI().getPath()
                              + "/"
                              + ((Identifiable) resourceModel).getId()));
            }
            entityModels.add(entityModel);
          } else {
            log.warn(
                "Detected non resource model type '{}' in controller response collection",
                l.getClass().getCanonicalName());
          }
        }

        collectionModel.setData(entityModels);
        return collectionModel;
      } else {
        return body;
      }
    } else {
      return body;
    }
  }
}
