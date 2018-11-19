<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">  
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<meta http-equiv="content-type" content="text/html;charset=utf-8"></meta>
<style type="text/css"> 
    body {
        font-family: SimSun;
    }
</style>
</head>
<body>
	<div>
		Hello ${name}
	    ${"${name}"}
	    ${r"${name}"}
		<#list ["A", "B", "C"] as item>
		  ${item}
		</#list>

		<#assign seq = ["A", "B", "C", "D", "E", "F", "G", "H"]>
		<#list seq[1..3] as i>${i}</#list>
		
		String Slicing
		<#assign s="ABCDEFGH">
		${s[2..3]}
		${s[2..<3]}
		${s[2..*3]}
		${s[2..]}
		
		${.now}
		<#if !sayHello>
		Hello
		</#if>
		<#assign url="someUrl?id=" + id >
		${url}
		someUrl?id=${id}
		someUrl?id=${id?c}
		${list[0].name}
		<#list list as item>
		${item.name}
		</#list>
		${3/2}
		${(3/2)?int}
		${name?length}
		<#--
		${repeat("Hello", 3)}
		-->
		${unexistName!"Simon"}
	</div>
</body>
