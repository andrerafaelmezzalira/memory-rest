angular.module('app', [ 'ngRoute' ]).config(function($routeProvider) {

	$routeProvider.when('/', {
		templateUrl : 'example.html',
	}).otherwise({
		redirectTo : '/'
	});
}).run(function($rootScope, $http) {

	$rootScope.$on("$routeChangeStart", function() {
		$http.post('rest/example', {
			mercado : {
				nome : 'Angeloni',
				dados : [ {
					abacate : 2.97,
					laranja : 2.15
				}, {
					abacate : 2.95,
					laranja : 2.18,
					mamao : 2.89
				} ]
			}
		}).success(function(data) {
			console.log(data)
		}).error(function() {
			console.log('ois4')
		});

		/*
		 * $http.get('rest/example').success(function(data) { //
		 * console.log(data) }).error(function() { console.log('ois2') });
		 * 
		 * 
		 * $http.get('rest/example', { params: { json: { usuario: {
		 *  } } } }).success(function(data) { console.log(data)
		 * }).error(function() { console.log('ois2') });
		 */
	});

});