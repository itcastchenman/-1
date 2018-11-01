<html>

<head>
	<meta charset="utf-8">
	<title>Freemarker入门小demo</title>
</head>

<body>
	<#include "head.ftl">

	<#--我只是一个注释，我不会有任何输出 -->
	${name},你好.${message}<br>
	
	<#assign linkman="周先生"><br>
	联系人：${linkman}<br>
	
	<#assign info={"moble":"13527571496","address":"北京市昌平区王府街"}>
	手机号码：${info.moble}<br>
	地址：${info.address}<br>
	
	<#-- 在这里=和==是一样的 -->
	<#if success=true>
		你已审核通过
		<#else>
		你未审核通过
	</#if>
	<br>
	
	----商品价格表----<br>
	
	<#list goodsList as goods>
		${goods_index+1} 商品名称：${goods.name} 商品价格：${goods.price}<br>
	</#list>
	
	<#-- 内建函数/内建函数语法格式： 变量+?+函数名称  -->
	<#assign text="{'brank':'ICBC','account':'123456789'}"/>
	<#assign data=text?eval/>
		开户银行：${data.brank} 账号：${data.account}<br>
	

	显示日期：${today?date}<br>
	显示时间：${today?time}<br>
	显示日期+时间：${today?datetime}<br>
	显示日期格式化：${today?string("yyyy年MM月")}<br>
	
	累计积分：${point}<br>
	<#-- 数字转为字符串 -->
	累计积分：${point?c}<br>
	
	<#-- 空值处理运算符 -->
	${bbb!'bbb没有被赋值'}
	<br>
</body>
</html>