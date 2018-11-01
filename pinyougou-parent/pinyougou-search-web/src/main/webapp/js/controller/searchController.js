app.controller('searchController',function($scope,$location,searchService){
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sort':'','sortField':''}//搜索对象
	
	$scope.search=function(){
		
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo)
		
		searchService.search($scope.searchMap).success(function(response){
			$scope.resultMap=response;
			buildPageLabel();
		});
	}
	
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		
		if(key=='category'||key=='brand' ||key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
		$scope.searchMap.pageNo=1;
		$scope.search();
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' ||key=='price'){//如果是分类或品牌
				$scope.searchMap[key]="";
			}else{//否则是规格
				delete $scope.searchMap.spec[key];//移除此属性
			}
		$scope.searchMap.pageNo=1;
		$scope.search();
	}
	
	//构建分页标签(totalPages 为总页数)
	buildPageLabel=function(){
		$scope.pageLable=[];//新增分页栏属性
		var maxPageNo=$scope.resultMap.totalPages;//总页数
		var firstPage=1;//开始页码是1
		var lastPage=maxPageNo;//尾页页码
		
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		if(maxPageNo>5){//如果总页数大于 5 页,显示部分页码
			
			if($scope.searchMap.pageNo<=3){//如果当前页小于等于 3
				$scope.firstDot=false;//前面没点
				lastPage=5; //前 5 页
			}else if($scope.searchMap.pageNo>=maxPageNo-2){//如果当前页大于等于最大页码-2
				firstPage=maxPageNo-4;//后 5 页
				$scope.lastDot=false;//后面没点
			}else{ //显示当前页为中心的 5 页
				
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{
			$scope.firstDot=false;//前面没点
			$scope.lastDot=false;//后面没点
		}
		
		//循环产生页码标签
		for(var i=firstPage;i<=lastPage;i++){
			$scope.pageLable.push(i);
		}
		
		
	}
	
	//根据页码查询
	$scope.queryByPage=function(pageNo){
		//页码验证
		if(pageNo<1||pageNo>$scope.resultMap.totalPages){
			return;
		}
		
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
		
	}
	
	//判断当前页是否为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否为尾页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
			
		}else{
			return false;
		}
	}
	
	//排序查询
	$scope.sortSearch=function(sort,sortField){
		$scope.searchMap.sort=sort;
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.pageNo=1;
		$scope.search();
	}
	
	//判断关键字是不是品牌
	$scope.keywordsIsBrand=function(){
		
		for(var i=0;i<$scope.resultMap.brandList.length;i++){
			
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果包含
				return true;
			}
		}
		return false;
	}
	
	//加载关键字
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords=$location.search()['keywords'];
		$scope.search();//查询
	}
	
});