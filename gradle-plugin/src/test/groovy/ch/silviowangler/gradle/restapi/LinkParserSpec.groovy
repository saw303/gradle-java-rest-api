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
package ch.silviowangler.gradle.restapi

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by Silvio Wangler on 27/01/16.
 */
class LinkParserSpec extends Specification {

    @Unroll
    void "Parse options.json link #version"() {

        when:
        def parser = new LinkParser(jsonLink, version)

        then:
        parser.toBasePath() == expectedLink
        parser.toEntityPath() == '{id}'
        parser.pathVariables.size() == expectedPathVariables.size()
        parser.pathVariables == expectedPathVariables
        parser.directEntity == expectedDirectEntity


        where:

        version || jsonLink                                                                                                                                || expectedLink                                                                                                                   || expectedPathVariables                                          || expectedDirectEntity
        '1'     || '/:version/partner/:entity'                                                                                                             || "/v1/partner"                                                                                                                  || []                                                             || false
        '2'     || '/:version/partner/:entity'                                                                                                             || "/v2/partner"                                                                                                                  || []                                                             || false
        '3'     || '/:version/partner/:entity'                                                                                                             || "/v3/partner"                                                                                                                  || []                                                             || false
        '4'     || '/:version/partner/:partner/adresse/:entity'                                                                                            || "/v4/partner/{partner}/adresse"                                                                                                || ['partner']                                                    || false
        '10'    || '/:version/partner/:partner/adresse/:adresse/winkel/:entity'                                                                            || "/v10/partner/{partner}/adresse/{adresse}/winkel"                                                                              || ['partner', 'adresse']                                         || false
        '20'    || '/:version/partner/:partner/adresse/:adresse/winkel'                                                                                    || "/v20/partner/{partner}/adresse/{adresse}/winkel"                                                                              || ['partner', 'adresse']                                         || true
        '1'     || '/:version/kunden/:kunde/angebote/:angebot/varianten/:variante/varianteversicherteobjekte/:entity'                                      || "/v1/kunden/{kunde}/angebote/{angebot}/varianten/{variante}/varianteversicherteobjekte"                                        || ['kunde', 'angebot', 'variante']                               || false
        '99'    || '/:version/kunden/:kunde/angebote/:angebot/varianten/:variante/varianteversicherteobjekte/:varianteversichertesobjekt/produkte/:entity' || "/v99/kunden/{kunde}/angebote/{angebot}/varianten/{variante}/varianteversicherteobjekte/{varianteversichertesobjekt}/produkte" || ['kunde', 'angebot', 'variante', 'varianteversichertesobjekt'] || false
    }
}
