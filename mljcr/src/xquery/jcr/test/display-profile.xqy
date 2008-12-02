xquery version "1.0-ml";

let $p := (
	for $doc in /prof:report
	order by $doc/prof:metadata/prof:overall-elapsed descending
	return $doc
)[1]

return xdmp:quote (
	<prof:report>{
		$p/prof:metadata,
		<prof:histogram>{
			for $e in $p/prof:histogram/prof:expression
			order by $e/prof:shallow-time descending
			return $e
		}</prof:histogram>
	}</prof:report>
)
