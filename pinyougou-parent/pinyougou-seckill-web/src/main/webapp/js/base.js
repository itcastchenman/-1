var app=angular.module('pinyougou',[]);

/*$sce 服务写成过滤器----高亮显示需要*/
app.filter('trustHtml',['$sce',function($sce){
	
	return function(data){
		return $sce.trustAsHtml(data);
	}
	
}])