/**
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
package ch.silviowangler.rest.micronaut;

import ch.silviowangler.rest.model.CollectionModel;
import ch.silviowangler.rest.model.EntityModel;
import ch.silviowangler.rest.model.ResourceModel;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.MediaType.APPLICATION_JSON_TYPE;

/**
 * @author Silvio Wangler
 */
public class JsonModelFilter implements HttpServerFilter {
	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
		return Flowable.fromPublisher(chain.proceed(request)).doOnNext(res -> {

			Optional<MediaType> contentType = res.getContentType();
			if (contentType.isPresent() && contentType.get().getSubtype().equals(APPLICATION_JSON_TYPE.getSubtype())) {

				if (res.body() instanceof ResourceModel) {

					ResourceModel model = (ResourceModel) res.body();
					((MutableHttpResponse) res).body(new EntityModel(model));

				} else if (res.body() instanceof List) {

					List models = (List) res.body();
					List<EntityModel> entityModels = new ArrayList<>(models.size());

					CollectionModel collectionModel = new CollectionModel();

					for (Object model : models) {
						if (model instanceof ResourceModel) {
							collectionModel.getData().add(new EntityModel((ResourceModel) model));
						}
					}
					collectionModel.setData(entityModels);

					((MutableHttpResponse) res).body(collectionModel);
				}
			}
		});
	}
}
