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
package ch.silviowangler.gradle.restapi.tasks

import ch.silviowangler.gradle.restapi.RestApiExtension
import ch.silviowangler.gradle.restapi.validation.AtLeastOneVerbValidator
import ch.silviowangler.gradle.restapi.validation.ConstraintViolation
import ch.silviowangler.gradle.restapi.validation.FieldTypeIsSupportedValidator
import ch.silviowangler.gradle.restapi.validation.MandatoryFieldValidator
import ch.silviowangler.gradle.restapi.validation.MinMaxValuesMatchTypeValidator
import ch.silviowangler.gradle.restapi.validation.OnlyOnePostVerbValidator
import ch.silviowangler.gradle.restapi.validation.Validator
import ch.silviowangler.rest.contract.model.v1.ResourceContract
import ch.silviowangler.rest.contract.model.v1.ResourceTypes
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

/**
 * @author Silvio Wangler
 */
class ValidationTask extends SpecificationBaseTask {

	private final List<Validator> validators

	private final RestApiExtension restApiExtension

	ValidationTask() {
		this.validators = [
			new AtLeastOneVerbValidator(),
			new FieldTypeIsSupportedValidator(),
			new MinMaxValuesMatchTypeValidator(),
			new MandatoryFieldValidator(),
			new OnlyOnePostVerbValidator()
		]
		this.restApiExtension = project.restApi as RestApiExtension
	}

	@TaskAction
	void validate() {
		List<File> specs = findSpecifications(getOptionsSource())

		Map<ResourceContract, Set<ConstraintViolation>> violationMap = [:]
		List<ResourceTypes> definedResourceTypes = []

		for (File specFile in specs) {
			ResourceContract contract = specGenerator.parseResourceContract(specFile, this.restApiExtension.getResponseEncoding() ?: Charset.forName("UTF-8")).resourceContract

			violationMap[contract] = new HashSet<>()

			for (Validator validator in validators) {
				violationMap[contract].addAll(validator.validate(contract, definedResourceTypes))
			}

			definedResourceTypes.addAll(contract.getTypes())
		}

		Collection<Set<ConstraintViolation>> violations = violationMap.values().findAll { c -> c.size() > 0 }
		boolean hasViolations = violations.size() > 0

		if (hasViolations) {
			println "Contract violations"
			println "===================\n"
			violations.each { set -> println '- ' + set.collect { v -> v.toString() }.join("\n") }
			throw new RuntimeException("Your specifications violate with the contract.")
		}
	}
}
