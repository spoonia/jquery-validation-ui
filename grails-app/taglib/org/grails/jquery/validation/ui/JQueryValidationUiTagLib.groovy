/* Copyright 2010-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.jquery.validation.ui

import groovy.util.slurpersupport.GPathResult
import org.springframework.util.ReflectionUtils
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import java.lang.reflect.Field

/**
 *
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
 *
 * @since 0.1
 */
class JQueryValidationUiTagLib {
	static namespace = "jqvalui"
	static final String TAG_ERROR_PREFIX = "Tag [jqvalui:renderValidationScript] Error: "

	def jqueryValidationService

	def applyCustomConstraint = {
		attrs, body ->
			if (!attrs.rule) throwTagError("Tag [applyCustomConstraint] is missing required attribute [rule]")
			if (!attrs.property) throwTagError("Tag [applyCustomConstraint] is missing required attribute [property]")
			if (!attrs.parameter) throwTagError("Tag [applyCustomConstraint] is missing required attribute [parameter]")

			out << """
				<constraint>
				\t\t\t\t<rule>${attrs.rule}</rule>
				\t\t\t\t<property>${attrs.property}</property>
				\t\t\t\t<parameter>${attrs.parameter}</parameter>
				"""
			if (attrs.message)
				out << "\t\t\t\t<message>${attrs.message}</message>"

			out << "</constraint>"
	}

	def applyUniqueConstraint = {
		attrs, body ->
			if (!attrs.domain) throwTagError("Tag [applyUniqueConstraint] is missing required attribute [domain]")
			if (!attrs.property) throwTagError("Tag [applyUniqueConstraint] is missing required attribute [property]")

			out << """
				<constraint>
				\t\t\t\t<rule>unique</rule>
				\t\t\t\t<property>${attrs.property}</property>
				\t\t\t\t<parameter>${attrs.domain}</parameter>
				"""
			if (attrs.message)
				out << "\t\t\t\t<message>${attrs.message}</message>"

			out << "</constraint>"
	}

	def applyCustomValidator = {
		attrs, body ->
			if (!attrs.domain) throwTagError("Tag [applyCustomValidator] is missing required attribute [domain]")
			if (!attrs.property) throwTagError("Tag [applyCustomValidator] is missing required attribute [property]")

			out << """
				<constraint>
				\t\t\t\t<rule>validator</rule>
				\t\t\t\t<property>${attrs.property}</property>
				\t\t\t\t<parameter>${attrs.domain}</parameter>
				"""
			if (attrs.message)
				out << "\t\t\t\t<message>${attrs.message}</message>"

			out << "</constraint>"
	}

	def resources = { attrs, body ->
		String type = attrs.remove("type")
		def packed
		if (!type) {
			packed = grailsApplication.config.jqueryValidationUi.qTip.get("packed", true)
			renderJavaScript g.resource(plugin: "jqueryValidationUi", dir: "js/qTip2",
					file: "jquery.qtip2.${packed ? 'pack.js' : 'js'}")
			renderJavaScript g.resource(plugin: "jqueryValidationUi", dir: "js/jquery-validation-ui", file: "grails-validation-methods.js")
			renderCSS g.resource(plugin: "jqueryValidationUi", dir: "css/qTip2", file: "jquery.qtip2.css")
		} else if (type.equals("js")) {
			packed = grailsApplication.config.jqueryValidationUi.qTip.get("packed", true)
			renderJavaScript g.resource(plugin: "jqueryValidationUi", dir: "js/qTip2",
					file: "jquery.qtip2.${packed ? 'pack.js' : 'js'}")
			renderJavaScript g.resource(plugin: "jqueryValidationUi", dir: "js/jquery-validation-ui", file: "grails-validation-methods.js")
		} else if (type.equals("css")) {
			renderCSS g.resource(plugin: "jqueryValidationUi", dir: "css/qTip2", file: "jquery.qtip2.css")
		}
	}

	def renderErrors = { attrs, body ->
		def renderErrorsOnTop = attrs.render ? Boolean.valueOf(attrs.render) : grailsApplication.config.jqueryValidationUi.get("renderErrorsOnTop", true)
		if (renderErrorsOnTop) {
			String qTipClasses = grailsApplication.config.jqueryValidationUi.qTip.classes ?: ""
			String style = attrs.remove("style") ?: ''
			String bodyText = body()
			def writer = getOut()
			writer << """
<div id="errorContainer${bodyText ? 'ServerSide' : ''}" style="z-index: 15001; opacity: 1; ${
				bodyText ? 'display: block' : 'display: none'
			}; position: relative; ${style}" class="ui-tooltip qtip ui-helper-reset ui-tooltip-focus ui-tooltip-pos-bc${
				qTipClasses ? " $qTipClasses" : ""
			}">
<div style="width: 12px; height: 12px; background-color: transparent; border: 0px none; left: 50%; margin-left: -6px; bottom: -12px;" class="ui-tooltip-tip">
</div>
<div class="ui-tooltip-wrapper">
<div class="ui-tooltip-content errors">"""
			if (bodyText) {
				writer << bodyText
			} else {
				writer << '<ul></ul>'
			}
			writer << """
</div>
</div>
</div>
"""
		}
	}

	def renderError = { attrs, body ->
		String labelFor = attrs.remove("for")
		if (!labelFor) {
			throwTagError("Tag [jqvalui:renderError] Error: Tag missing required attribute [for]")
		}
		def renderErrorsOnTop = grailsApplication.config.jqueryValidationUi.get("renderErrorsOnTop", true)
		if (!renderErrorsOnTop) {
			String style = attrs.remove("style") ?: ''
			String qTipClasses = grailsApplication.config.jqueryValidationUi.qTip.classes ?: ""
			def writer = getOut()
			writer << """
<div style="z-index: 15001; opacity: 1; display: block; ${
				style
			}" class="ui-tooltip qtip ui-helper-reset ui-tooltip-focus ui-tooltip-pos-lc${
				qTipClasses ? " $qTipClasses" : ""
			}">
<div style="width: 12px; height: 12px; background-color: transparent; border: 0px none; top: 50%; margin-top: -6px; left: -12px;" class="ui-tooltip-tip">
<div style="border-right: 12px solid ${getConnectorColor()}; border-top: 6px dashed transparent; border-bottom: 6px dashed transparent;" class="ui-tooltip-tip-inner">
</div>
</div>
<div class="ui-tooltip-wrapper">
  <div class="ui-tooltip-content">
     <label style="display: block;" for="${labelFor}">"""
			writer << body()
			writer << """</label>
  </div>
</div>
</div>
"""
		}
	}

	private String getConnectorColor() {
		def jQueryUiStyle = grailsApplication.config.jqueryValidationUi.qTip.get("jQueryUiStyle", false)
		return "rgb(217, 82, 82)"
	}

	private renderJavaScript(def url) {
		out << '<script type="text/javascript" src="' + url + '"></script>\n'
	}

	private renderCSS(def url) {
		out << '<link rel="stylesheet" type="text/css" media="screen" href="' + url + '" />\n'
	}

	def renderValidationScript = { attrs, body ->
		String xmlContent = body()
		Set<CustomConstraintEntry> constraintEntries = []
		if (xmlContent) {
			xmlContent = "<constraints>${xmlContent}</constraints>"
			GPathResult constraints = new XmlSlurper().parseText(xmlContent)
			constraints.constraint.each {
				constraint ->
					CustomConstraintEntry entry =
							new CustomConstraintEntry(
									rule: constraint.rule,
									property: constraint.property,
									parameter: constraint.parameter,
									message: constraint.message)
					if (!entry.rule) {
						throw new Exception('<rule></rule> tag missing from custom constraint body!')
					}
					if (!entry.property) {
						throw new Exception('<property></property> tag missing from custom constraint body!')
					}
					if (!entry.parameter) {
						throw new Exception('<parameter></parameter> tag missing from custom constraint body!')
					}

					constraintEntries << entry
			}
		}
		String forClass = attrs.remove("for")
		def alsoProperties = attrs.remove("also")
		def notProperties = attrs.remove("not")
		String form = attrs.remove("form")
		def config = grailsApplication.config.jqueryValidationUi
		def jQueryUiStyle = config.qTip.get("jQueryUiStyle", false)
		String qTipClasses = config.qTip.classes ?: ""
		String errorClass = attrs.errorClass ?: config.errorClass ?: "error"
		String validClass = attrs.validClass ?: config.validClass ?: "valid"
		String errorContainer = attrs.errorContainer ?: config.errorContainer ?: "#errorContainer"
		String errorLabelContainer = attrs.errorLabelContainer ?: config.errorLabelContainer ?: "div.errors ul"
		String errorElement = attrs.errorElement ?: config.errorElement ?: "label"
		String errorWrapper = attrs.errorWrapper ?: config.errorWrapper ?: "li"


		def onsubmit = attrs.onsubmit ? Boolean.valueOf(attrs.onsubmit) : config.get("onsubmit", true)
		def onkeyup = attrs.onkeyup ?: config.get("onkeyup", false)
		def qtip = attrs.qtip ? Boolean.valueOf(attrs.qtip) : config.get("qtip", false)

		def submitHandler = attrs.remove("submitHandler") ?: config.submitHandler ?: null
		def highlightHandler = attrs.remove("highlight") ?: config.highlight ?: null
		def unhighlightHandler = attrs.remove("unhighlight") ?: config.unhighlight ?: null


		def renderErrorsOnTop = attrs.renderErrorsOnTop ? Boolean.valueOf(attrs.renderErrorsOnTop) : config.get("renderErrorsOnTop", true)
		String renderErrorsOptions = ""
		Locale locale = RCU.getLocale(request)

		if (!forClass) {
			throwTagError("${TAG_ERROR_PREFIX}Tag missing required attribute [for]")
		}
		def validatableClass = grailsApplication.classLoader.loadClass(forClass)
		if (!validatableClass) {
			throwTagError("${TAG_ERROR_PREFIX}Invalid validatableClass defined in attribute [for], $validatableClassName not found!")
		}

		if (notProperties) {
			notProperties = notProperties.split(',').collect { it.trim() }
		}
		if (alsoProperties) {
			alsoProperties = alsoProperties.split(',').collect { it.trim() }
			if (notProperties) {
				notProperties.addAll(alsoProperties)
			} else {
				notProperties = new ArrayList(alsoProperties)
			}
		}

		if (renderErrorsOnTop) {
			renderErrorsOptions = """
errorContainer: '$errorContainer',
errorLabelContainer: '$errorLabelContainer',
wrapper: '$errorWrapper',
"""
		} else if (qtip) {
			renderErrorsOptions = """
success: function(label)
{
	\$('[id="' + label.attr('for') + '"]').qtip('destroy');
},
errorPlacement: function(error, element)
{
	if (\$(error).text())
	\$(element).filter(':not(.${validClass})').qtip({
		overwrite: true,
		content: error,
		position: { my: 'left center', at: 'right center' },
		show: {
			event: false,
			ready: true
		},
		hide: false,
		style: {
			widget: ${jQueryUiStyle},
			classes: '${qTipClasses}',
			tip: true
		}
	});
},
"""
		}

		ConstrainedPropertiesEntry rootConstrainedPropertiesEntry = new ConstrainedPropertiesEntry(validatableClass: validatableClass)
		rootConstrainedPropertiesEntry.constrainedProperties = jqueryValidationService.getConstrainedProperties(validatableClass)
		if (notProperties) {
			def rootNotProperties = notProperties.findAll { it.indexOf('.') == -1 }
			notProperties.removeAll(rootNotProperties)
			rootConstrainedPropertiesEntry.constrainedProperties = rootConstrainedPropertiesEntry.constrainedProperties.findAll { k, v -> !rootNotProperties.contains(k) }
		}
		def constrainedPropertiesEntries = [rootConstrainedPropertiesEntry]
		ConstrainedPropertiesEntry childConstrainedPropertiesEntry
		def childNotProperties
		Class childValidatableClass
		alsoProperties.each { propertyName ->
			childValidatableClass = findField(validatableClass, propertyName).type
			childConstrainedPropertiesEntry = new ConstrainedPropertiesEntry(namespace: propertyName, validatableClass: childValidatableClass)
			childConstrainedPropertiesEntry.constrainedProperties = jqueryValidationService.getConstrainedProperties(childValidatableClass)
			if (notProperties) {
				childNotProperties = notProperties.collect {
					it.indexOf(propertyName) != -1 ? it.substring(propertyName.length() + 1) : null
				}
				notProperties.removeAll(childNotProperties)
				childConstrainedPropertiesEntry.constrainedProperties = childConstrainedPropertiesEntry.constrainedProperties.findAll { k, v -> !childNotProperties.contains(k) }
			}
			constrainedPropertiesEntries << childConstrainedPropertiesEntry
		}

		String rules = jqueryValidationService.createJavaScriptConstraints(constrainedPropertiesEntries, constraintEntries.asList(), locale)
		String messages = jqueryValidationService.createJavaScriptMessages(constrainedPropertiesEntries, constraintEntries.asList(), locale)
		out << render(plugin: 'jqueryValidationUi', template: '/taglib/renderValidationScript',
				model: [
						form              : form, onkeyup: onkeyup, errorClass: errorClass,
						errorElement      : errorElement, validClass: validClass, onsubmit: onsubmit,
						submitHandler     : submitHandler, highlightHandler: highlightHandler,
						unhighlightHandler: unhighlightHandler, renderErrorsOptions: renderErrorsOptions,
						rules             : rules, messages: messages
				])
	}

	private static Field findField(Class clazz, String name) {
		Field field = null

		if (name.indexOf('.') == -1) {
			field = ReflectionUtils.findField(clazz, name)
		} else {
			Class declaringClass = clazz
			def fieldNames = name.split("\\.")
			for (fieldName in fieldNames) {
				field = ReflectionUtils.findField(declaringClass, fieldName)
				if (!field) {
					throw new IllegalArgumentException("Property $name invalid!")
				}
				declaringClass = field.type
			}
		}
		return field
	}
}


