xquery version "1.0-ml";

declare function local:slowest10-uris()
	as xs:string*
{
	(
		for $meta in fn:doc()/prof:report/prof:metadata
		order by $meta/prof:overall-elapsed descending
		return xdmp:node-uri ($meta)
	)[1 to 10]
};

declare function local:profile-doc ($uri as xs:string)
	as element(prof:report)
{
	fn:doc ($uri)/prof:report
};

declare variable $slowest-uris := local:slowest10-uris();
declare variable $slowest := $slowest-uris[1];

declare variable $prof-doc := local:profile-doc ($slowest);

declare function local:format-header ($meta as element(prof:metadata)?)
{
	if ($meta)
	then fn:concat ("Total time: ", fn:string ($meta/prof:overall-elapsed), " (", $slowest, ")")
	else "No profile data"
};

declare function local:format-histogram ($hist as element(prof:histogram)?)
{
	if (fn:empty ($hist))
	then "No historgram data"
	else
		for $e in $hist/prof:expression
		order by $e/prof:shallow-time descending
		return
		<div class="exprblock">
			<div>
				Line <b>{fn:data ($e/prof:line)}</b>
				({fn:data ($e/prof:count)} times)
			</div>
			<div>
				<b>{fn:data ($e/prof:shallow-time)}</b><sub>shallow</sub> {fn:data ($e/prof:deep-time)}<sub>deep</sub>
			</div>
			<div class="codelisting">
				{fn:string ($e/prof:expr-source)}
			</div>
		</div>
};

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Profile Helper</title>
<link rel="stylesheet" type="text/css" href="prof.css"/></head>
<body>
<div class="headerblock">{
	local:format-header ($prof-doc/prof:metadata)
}</div>
<div class="histogramblock">{
	local:format-histogram ($prof-doc/prof:histogram)
}</div>
</body>
</html>

