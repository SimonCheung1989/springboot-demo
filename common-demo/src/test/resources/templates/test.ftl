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