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
}