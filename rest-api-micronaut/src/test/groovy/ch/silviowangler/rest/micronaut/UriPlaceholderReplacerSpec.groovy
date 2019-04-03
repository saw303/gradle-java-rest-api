package ch.silviowangler.rest.micronaut

import io.micronaut.web.router.UriRouteMatch
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Silvio Wangler
 */
class UriPlaceholderReplacerSpec extends Specification {

    @Subject
    ExpandedGetResponseFilter.UriPlaceholderReplacer uriPlaceholderReplacer = new ExpandedGetResponseFilter.UriPlaceholderReplacer()


    void "replace one placeholder placeholder"() {

        given:
        UriRouteMatch uriRouteMatch = Mock()

        when:
        String result = uriPlaceholderReplacer.replacePlaceholders('/v1/countries/{:entity}/cities/', uriRouteMatch)

        then:
        result == '/v1/countries/CHE/cities/'

        and:
        1 * uriRouteMatch.getVariableValues() >> ['id': 'CHE']
        1 * uriRouteMatch.getArgumentNames() >> ['id']
        0 * _
    }

    void "replace several placeholders placeholder"() {

        given:
        UriRouteMatch uriRouteMatch = Mock()

        when:
        String result = uriPlaceholderReplacer.replacePlaceholders('/v1/countries/{:country}/cities/{:entity}/municipalities', uriRouteMatch)

        then:
        result == '/v1/countries/CHE/cities/ZH/municipalities'

        and:
        1 * uriRouteMatch.getVariableValues() >> ['id': 'ZH', 'country': 'CHE']
        1 * uriRouteMatch.getArgumentNames() >> ['country', 'id']
        0 * _
    }

}
