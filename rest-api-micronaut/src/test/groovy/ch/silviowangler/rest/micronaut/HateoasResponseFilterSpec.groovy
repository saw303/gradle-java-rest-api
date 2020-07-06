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
import io.micronaut.web.router.UriRouteMatch
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

    when:
    hateoasResponseFilter.enrichHateoasData(request, response)
    List<ResourceLink> enrichedLinks = response.body()["links"]

    then: "ensure parameters are encoded correctly"
    enrichedLinks.find { it["rel"] == "first" }["href"].toString() == expectedPageLink
    enrichedLinks.find { it["rel"] == "last" }["href"].toString() == expectedPageLink
  }
}
