package org.grails.jquery.validation.ui

class ConstrainedPropertiesEntry {
	String namespace
	String domain
	Class validatableClass
	Map constrainedProperties
}

class CustomConstraintEntry {
	String rule
	String property
	String parameter
	String message

	boolean equals(o) {
		if (this.is(o)) return true
		if (!(o instanceof CustomConstraintEntry)) return false

		CustomConstraintEntry that = (CustomConstraintEntry) o

		if (parameter != that.parameter) return false
		if (property != that.property) return false
		if (rule != that.rule) return false

		return true
	}

	int hashCode() {
		int result
		result = rule.hashCode()
		result = 31 * result + property.hashCode()
		result = 31 * result + parameter.hashCode()
		return result
	}


	@Override
	public String toString() {
		return "CustomConstraintEntry{" +
				"rule='" + rule + '\'' +
				", property='" + property + '\'' +
				", parameter='" + parameter + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}