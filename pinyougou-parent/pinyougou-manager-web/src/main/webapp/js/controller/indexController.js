app.controller('indexController',function($scope,$controller,loginService){
	
	$controller('baseController',{$scope:$scope});//继承,通过baseController引入
	
	$scope.showLoginName=function(){
		loginService.loginName().success(
				function(response){
			$scope.loginName=response.loginName;
		})
	}
	
});