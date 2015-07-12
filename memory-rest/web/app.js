angular.module('app', [ 'ngRoute' ]).config(function($routeProvider) {

	$routeProvider.when('/', {
		templateUrl : 'example.html',
	}).otherwise({
		redirectTo : '/'
	});
}).run(function($rootScope, $http) {

	$rootScope.$on("$routeChangeStart", function() {
		$http.post('rest/example', {
			usuario : {
				cpf : 2,
				login : "showli"
			}
		}).success(function(data) {
			console.log(data)
		}).error(function() {
			console.log('ois4')
		});
		
		
		/*$http.get('rest/example').success(function(data) {
			// console.log(data)
		}).error(function() {
			console.log('ois2')
		});
		
		
		$http.get('rest/example', {
			params: {
				json: {
					usuario: {
						
					}
				}
			}
		}).success(function(data) {
			 console.log(data)
		}).error(function() {
			console.log('ois2')
		});
		*/
	});

});