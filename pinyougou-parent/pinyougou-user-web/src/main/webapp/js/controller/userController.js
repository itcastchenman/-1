 //控制层 
app.controller('userController' ,function($scope,userService){	
	
	$scope.entity={};
	
	//注册
	$scope.reg=function(){
		
		if($scope.entity.password!=$scope.password){
			alert("两次输入的密码不一致，请重新输入");
			 return ;
		}
		//判断手机号是否输入
		//判断验证码是否输入
		
		userService.add($scope.entity,$scope.smscode).success(
				function(response){
					alert(response.message);
				})
		
	}
	
	$scope.sendCode=function(){
		
		if($scope.entity.phone==null){
			
			alert("请输入手机号！");
			return ;
		}
		
		userService.sendCode($scope.entity.phone).success(
				function(response){
					
					alert(response.message);
				});
		}

    
});	
