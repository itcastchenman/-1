app.controller('seckillController',function($scope,$location,$interval,seckillGoodsService){
	
	//读取列表数据绑定到表单中
	$scope.findList=function(){
		
		seckillGoodsService.findList().success(
				function(response){
					$scope.list=response;
				
				}
		);
	}
	
	//查询秒杀商品详情
	$scope.findOne=function(){
		seckillGoodsService.findOne($location.search()['id']).success(
				function(response){
					$scope.entity=response;
					
					allsecond=Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);//总秒数
					
					time=$interval(function(){
						if(allsecond>0){
							
							allsecond=allsecond-1;
							$scope.timeString=convertTimeString(allsecond);//转换时间字符串
							
						}else{
							
							$interval.canel(time);
							alert("秒杀服务已结束");
						}
						
						
					},1000);
					
				}
		);
	}
	
	//转换秒为 天小时分钟秒格式 XXX 天 10:22:33
	convertTimeString=function(second){
		
		var days=Math.floor(second/(60*60*24));//天数
		
		var hours=Math.floor( (second-days*60*60*24)/(60*60));//小时数
		
		var minutes=Math.floor((second-days*60*60*24-hours*60*60)/60);//分钟数
		
		var seconds=second-days*60*60*24-hours*60*60-minutes*60;//秒数
		
		var timeString="";
		
		if(days>0){
			timeString=days+"天";
		}
		
		return timeString+hours+":"+minutes+":"+seconds;
		
	}


	//提交订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
				function(response){
					if(response.success){//提交成功则跳转到支付页面
						alert("下单成功，请在 1 分钟内完成支付");
						location.href='pay.html'
					}else{
						alert(response.message)
					}
				}
		);
	}
	
});