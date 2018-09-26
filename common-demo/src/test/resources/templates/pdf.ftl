<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">  
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>${title}</title>
<meta http-equiv="content-type" content="text/html;charset=utf-8"></meta>
<style type="text/css"> 
    body {
        font-family: Arial Unicode MS;
    }
</style>
</head>
<body>
<#list ["A", "B", "C", "D"] as item>
  ${item}
</#list>

<#list 1..10 as i>
<h1>Hello 你好 妳好 ${i}</h1>
</#list>
</body>
</html>