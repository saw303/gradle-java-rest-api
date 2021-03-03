package ch.silviowangler.rest.micronaut

import ch.silviowangler.rest.model.ResourceLink
import ch.silviowangler.rest.model.pagination.DefaultPage
import ch.silviowangler.rest.model.pagination.DefaultPageable
import ch.silviowangler.rest.model.pagination.Slice
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpRequestFactory
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.web.router.UriRouteMatch
import io.reactivex.Flowable
import spock.lang.Specification
import spock.lang.Subject

class HateoasResponseFilterSpec extends Specification {

  @Subject
  HateoasResponseFilter hateoasResponseFilter = new HateoasResponseFilter([], "/api")

  void "Ensure parameter encoding works as expected"() {

    given: "a request"
    HttpRequest<?> request = HttpRequestFactory.INSTANCE.get("/api/endpoint")
    request.parameters.add("param1", "hello world")
    request.parameters.add("param2", "this?is=a test")

    and: "a response"
    MutableHttpResponse<?> response = HttpResponseFactory.INSTANCE.ok()
    Slice model = new DefaultPage([], new DefaultPageable(0, 10), 10)
    UriRouteMatch uriRouteMatch = Mock()
    _ * uriRouteMatch.getUri() >> "/endpoint"
    _ * uriRouteMatch.getProduces() >> [MediaType.APPLICATION_JSON_TYPE]
    response.attributes.put(HttpAttributes.ROUTE_MATCH.toString(), uriRouteMatch)
    response.body(model)

    and:
    String expectedPageLink = "/api/endpoint?page=0&limit=10&param1=hello+world&param2=this%3Fis%3Da+test"

    and:
    ServerFilterChain chain = Mock()
    _ * chain.proceed(_) >> { Flowable.just(response) }

    when:
    MutableHttpResponse<?> filteredResponse = Flowable.fromPublisher(hateoasResponseFilter.doFilter(request, chain)).blockingSingle()
    List<ResourceLink> enrichedLinks = filteredResponse.body()["links"] as List<ResourceLink>

    then: "ensure parameters are encoded correctly"
    enrichedLinks.find { it["rel"] == "first" }["href"].toString() == expectedPageLink
    enrichedLinks.find { it["rel"] == "last" }["href"].toString() == expectedPageLink
  }

  void "Ensure parameter with same name can exist multiple times"() {

    given: "a request"
    HttpRequest<?> request = HttpRequestFactory.INSTANCE.get("/api/endpoint")
    request.parameters.add("status", ["NEW", "MODIFIED"])

    and: "a response"
    MutableHttpResponse<?> response = HttpResponseFactory.INSTANCE.ok()
    Slice model = new DefaultPage([], new DefaultPageable(0, 10), 10)
    UriRouteMatch uriRouteMatch = Mock()
    _ * uriRouteMatch.getUri() >> "/endpoint"
    _ * uriRouteMatch.getProduces() >> [MediaType.APPLICATION_JSON_TYPE]
    response.attributes.put(HttpAttributes.ROUTE_MATCH.toString(), uriRouteMatch)
    response.body(model)

    and:
    String expectedPageLink = "/api/endpoint?page=0&limit=10&status=NEW&status=MODIFIED"

    and:
    ServerFilterChain chain = Mock()
    _ * chain.proceed(_) >> { Flowable.just(response) }

    when:
    MutableHttpResponse<?> filteredResponse = Flowable.fromPublisher(hateoasResponseFilter.doFilter(request, chain)).blockingSingle()
    List<ResourceLink> enrichedLinks = filteredResponse.body()["links"] as List<ResourceLink>

    then: "ensure both parameters exist"
    enrichedLinks.find { it["rel"] == "first" }["href"].toString() == expectedPageLink
    enrichedLinks.find { it["rel"] == "last" }["href"].toString() == expectedPageLink
  }

  void "Ensure parameters exist in self link"() {

    given: "a request"
    HttpRequest<?> request = HttpRequestFactory.INSTANCE.get("/api/endpoint")
    request.parameters.add("param1", "hello world")
    request.parameters.add("status", ["NEW", "MODIFIED"])

    and: "a response"
    MutableHttpResponse<?> response = HttpResponseFactory.INSTANCE.ok()
    Slice model = new DefaultPage([], new DefaultPageable(0, 10), 10)
    UriRouteMatch uriRouteMatch = Mock()
    _ * uriRouteMatch.getUri() >> "/endpoint"
    _ * uriRouteMatch.getProduces() >> [MediaType.APPLICATION_JSON_TYPE]
    response.attributes.put(HttpAttributes.ROUTE_MATCH.toString(), uriRouteMatch)
    response.body(model)

    and:
    String expectedPageLink = "/api/endpoint?page=0&limit=10&param1=hello+world&status=NEW&status=MODIFIED"

    and:
    ServerFilterChain chain = Mock()
    _ * chain.proceed(_) >> { Flowable.just(response) }

    when:
    MutableHttpResponse<?> filteredResponse = Flowable.fromPublisher(hateoasResponseFilter.doFilter(request, chain)).blockingSingle()
    List<ResourceLink> enrichedLinks = filteredResponse.body()["links"] as List<ResourceLink>

    then: "ensure self link parameters exist"
    enrichedLinks.find { it["rel"] == "self" }["href"].toString() == expectedPageLink
  }
}
