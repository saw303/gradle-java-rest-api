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
package ch.silviowangler.rest.micronaut;

import ch.silviowangler.rest.contract.model.v1.ResourceContract;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ContractReader {

  private static final Logger log = LoggerFactory.getLogger(ContractReader.class);

  private final ObjectMapper objectMapper;
  private final ConcurrentMap<Class, ResourceContract> cache;

  public ContractReader(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.cache = new ConcurrentHashMap<>();
  }

  public Optional<ResourceContract> fetchContract(Object resourceBean) {

    Class<?> key = resourceBean.getClass();

    try {
      return readContract(key);

    } catch (IllegalAccessException | NoSuchFieldException | IOException ex) {
      log.error("Unable to read contract from class '{}'", resourceBean.getClass().getSimpleName());
      return Optional.empty();
    }
  }

  public Optional<ResourceContract> fetchContract(Class<?> clazz) {

    try {
      return readContract(clazz);

    } catch (IllegalAccessException | NoSuchFieldException | IOException ex) {
      log.error("Unable to read contract from class '{}'", clazz.getSimpleName());
      return Optional.empty();
    }
  }

  private Optional<ResourceContract> readContract(Class<?> clazz)
      throws NoSuchFieldException, IllegalAccessException, IOException {
    ResourceContract resourceContract = cache.get(clazz);
    if (resourceContract == null) {
      resourceContract = doRead(clazz);
      cache.put(clazz, resourceContract);
    }
    return Optional.of(resourceContract);
  }

  private ResourceContract doRead(Class<?> clazz)
      throws NoSuchFieldException, IllegalAccessException, IOException {
    Field contractField = clazz.getField("OPTIONS_CONTENT");
    String json = (String) contractField.get(null);

    ResourceContract contract = objectMapper.readValue(json, ResourceContract.class);

    // fixup xRoute
    JsonNode rootNode = objectMapper.readTree(json);
    JsonNode xRoute = rootNode.get("general").get("x-route");
    contract.getGeneral().setxRoute(xRoute.asText());

    return contract;
  }
}
