<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Freemarker 测试</title>
</head>
<body>
<#--这是 freemarker 注释，不会输出到文件中-->
<h1>${name}； ${message}</h1>
<br>
<hr>
<br>
assign指令<br/>
<#--assign指令使用-->
<#assign linkman="黑马"/>
${linkman}<br>

<#assign info={"mobile":"13333333333", "address":"吉山村"} />
mobile = ${info.mobile}；address = ${info.address}

<br>
<hr>
<br>
include引入其他模板<br>
<#include "header.ftl"/>

<br>
<hr>
<br>
if条件控制语句<br>
<#assign bool=true>
<#if bool>
    bool的值为true
<#else>
    bool的值为false
</#if>

<br>
<hr>
<br>
list循环控制语句<br>
<#list goodsList as goods>
    ${goods_index}--${goods.name}--${goods.price}<br>
</#list>
总有${goodsList?size}条记录

<br>
<hr>
<br>
eval内建函数,可以将Json字符串转换为对象<br>
<#assign jsonStr='{"id":123,"name":"齐天大圣"}'/>
<#assign jsonObj=jsonStr?eval/>
${jsonObj.id}--${jsonObj.name}

<br>
<br>
日期格式化:<br>
.now 表示当前日期时间:${.now}<br>
today的日期时间: ${today?datetime}<br>
today的日期: ${today?date}<br>
today的时间: ${today?time}<br>
today的格式化显示: ${today?string("yyyy年MM月dd日 HH:mm:ss")}<br>

<br><br>
number数值默认显示 = ${number}; 可以使用?c方式进行格式化为字符串显示二不会出现千分位上使用,的方式: ${number?c}

<br>
<hr>
<br>
空值的处理<br>
<br>
值为空可以使用!表示都不显示${emp!}; 如果值为空以后显示具体的值则可以!"要显示的值"--> ${emp!"emp的值为空"}

<br>
<br>
???前面两个??表示一个变量是否存在;如果存在则返回true,否则false,后面一个?表示函数的调用. <br/>

<#assign bool2=false/>
${bool2???string}

<br>

<#if str3??>
    str3存在
<#else>
    str3不存在
</#if>

<br>
<hr>
<br>
</body>
</html>