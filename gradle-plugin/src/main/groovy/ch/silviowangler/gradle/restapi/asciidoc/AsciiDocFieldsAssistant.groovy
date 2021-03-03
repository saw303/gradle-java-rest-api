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
package ch.silviowangler.gradle.restapi.asciidoc

import ch.silviowangler.rest.contract.model.v1.ResourceField

/**
 * @author Osian Hughes (osian.hughes@onstructive.ch)
 */
class AsciiDocFieldsAssistant {

	/**
	 * |${field.name}* |
	 * ${field.type}* |
	 * ${field.options == null ? "" : field.options}* |
	 * ${field.mandatory.isEmpty() ? "" : field.mandatory}* |
	 * ${field.min == null ? "" : field.min}* |
	 * ${field.max == null ? "" : field.max}* |
	 * ${field.multiple}* |
	 * ${field.defaultValue == null ? "" : field.defaultValue}* |
	 * ${field.shield == null ? "" : field.shield}* |
	 * ${field.visible}* |
	 * ${field.sortable}* |
	 * ${field.readonly}* |
	 * ${field.filterable}* |
	 * ${field.alias.isEmpty() ? "" : field.alias}* |
	 * ${field.xComment}
	 */

	static final Collection<String> ALL_COLUMNS = [
		'name',
		'type',
		'options',
		'mandatory',
		'min',
		'max',
		'multiple',
		'defaultValue',
		'shield',
		'visible',
		'sortable',
		'readonly',
		'filterable',
		'alias',
		'xComment'
	]
	static final Map<String, String> TITLES = [
		'name': 'Name',
		'type': 'Data Type',
		'options': 'Options',
		'mandatory': 'Mandatory',
		'min': 'Minimum',
		'max': 'Maximum',
		'multiple': 'multiple',
		'defaultValue': 'Default Value',
		'shield': 'Shield',
		'visible': 'Visible',
		'sortable': 'Sortable',
		'readonly': 'Read Only',
		'filterable': 'Filterable',
		'alias': 'Alias',
		'xComment': 'Comment',
	]

	final Collection<ResourceField> fields
	final Set<String> columns = new LinkedHashSet<>()

	AsciiDocFieldsAssistant(Collection<ResourceField> fields) {
		this.fields = fields
		fields.collect {fld ->
			ALL_COLUMNS.collect { col ->
				if (fieldHasContent(fld[col])) {
					columns.add(col)
				}
			}
		}
		columns = columns.sort { col -> ALL_COLUMNS.indexOf(col) }
	}

	boolean fieldHasContent(Object value) {
		if (value != null) {
			if (value instanceof Collection || value instanceof String) {
				return !value.isEmpty()
			}
		}
		return value != null
	}

	Collection<String> getColumns() {
		return columns
	}

	def getValueFor(ResourceField field, String columnName) {
		return fieldHasContent(field[columnName]) ? field[columnName] : ''
	}
}
