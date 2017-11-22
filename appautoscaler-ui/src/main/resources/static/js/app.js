
	
	var myApp = angular.module('pcfm', [ 'ngRoute', 'ngMaterial' ]).
	config(function($routeProvider, $httpProvider) { // provider-injector
	  // This is an example of config block.
	  // You can have as many of these as you want.
	  // You can only inject Providers (not instances)
	  // into config blocks.
	}).
	run(function($rootScope, $http) { // instance-injector
	  // This is an example of a run block.
	  // You can have as many of these as you want.
	  // You can only inject instances (not Providers)
	  // into run blocks
		console.info("running....");
		
		$http.get("/spaceApps").success(function(data){
			//$rootScope.cloudinfo = data;
			console.info("before set")
			renderMessagesList(data,"cjd");
			console.info("after set")
		  }); 
	});
	
	myApp.controller('MyCtrl', function ($scope, $http, $httpParamSerializerJQLike) {
	    $scope.anyFunc = function (appGUID, spaceGUID, ruleURL, ruleAPIKey, ruleExpression, minInstances, maxInstances) {
	  
	    	var formData = $httpParamSerializerJQLike({
	    	    "rule":{
	    	      "appGUID":appGUID,
	    	      "spaceGUID":spaceGUID,
	    	      "ruleURL": ruleURL,
	    	      "ruleAPIKey" : ruleAPIKey,
	    	      "ruleExpression":ruleExpression,
	    	      "minInstances":minInstances,
	    	      "maxInstances":maxInstances
	    	    }
	    	  });
	    	
            var config = {
                    headers : {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
                    }
                }
	    	
			$http.post("/addRule", formData, config).success(function(data){
				//$rootScope.cloudinfo = data;
				console.info("addRule")
			  }); 
			
	        return "cjd-tre";
	    }});
	
	myApp.controller('RuleCtrl', function ($scope, $http, $httpParamSerializerJQLike) {
	    $scope.anyFunc = function (ruleURL) {
	    	
	    	console.log("check rule url")
	    	var returnData;
            $http.get("/checkRuleURL", {
                params: { ruleURL: ruleURL }
            }).then(successCallback, errorCallback)
			
	        
	    }});

	
	