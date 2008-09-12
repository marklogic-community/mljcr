xquery version "1.0-ml";

import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "../lib/fs-lib.xqy";

declare variable $uri := "jackrabbit/repository/default/data/dc1dce46-bcaa-4ab5-9355-a8bde270435d/%7bhttp%3a%2f%2fwww.jcp.org%2fjcr%2f1.0%7ddata.blob";

declare function local:doc-length ($uri as xs:string) as xs:integer
{
	let $node := fn:doc ($uri)/node()[1]
	let $node-kind := xdmp:node-kind ($node)
	let $str := if ($node-kind = "binary")
			then fn:string (xs:hexBinary ($node))
			else xdmp:quote ($node)
	let $len := if ($node-kind = "binary")
			then fn:string-length ($str) idiv 2
			else fn:string-length ($str)

	return $len
};


local:doc-length ($uri)
