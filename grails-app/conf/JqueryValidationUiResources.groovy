modules = {
	'jquery-validation-ui' {
		dependsOn 'jquery, jquery-validate'
		resource id: "validation-methods", url: [plugin: 'jqueryValidationUi', dir: 'js/jquery-validation-ui', file: 'grails-validation-methods.js']
	}

	'jquery-validation-ui-qtip2' {
		dependsOn 'jquery-validation-ui'
		resource id: "qtip", url: [plugin: 'jqueryValidationUi', dir: 'js/qTip2', file: 'jquery.qtip2.pack.js']
		resource id: "qtip-theme", url: [plugin: 'jqueryValidationUi', dir: 'css/qTip2', file: 'jquery.qtip2.css']
	}
}
