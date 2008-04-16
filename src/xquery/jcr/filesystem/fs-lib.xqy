xquery version "1.0-ml";

module namespace jcrfslib="http://marklogic.com/jcr/fs";


(: TODO: look at properties for pre-existing length property :)
declare function doc-length ($uri as xs:string) as xs:integer
{
	let $node := fn:doc ($uri)/node()[1]
	let $node-kind := xdmp:node-kind ($node)
	let $str := xdmp:quote ($node)
	let $len := if ($node-kind = "binary")
			then fn:string-length ($str) idiv 2
			else fn:string-length ($str)

	return $len
};

(: -------------------------------------------------------------- :)

declare private function dir-uri ($uri as xs:string) as xs:string
{
	if (fn:ends-with ($uri, "/"))
	then $uri
	else fn:concat ($uri, "/")
};

declare function is-directory ($uri as xs:string) as xs:boolean
{
        let $dir-uri := dir-uri ($uri)

        return
        fn:exists (xdmp:document-properties ($dir-uri)//prop:directory)
};

declare function directory-child-count ($uri as xs:string) as xs:integer
{
	let $dir-uri := dir-uri ($uri)

	return
	if (fn:not (is-directory ($dir-uri)))
	then 0
	else fn:count (xdmp:directory ($dir-uri, "1"))
};

(: FIXME: Need to list directory names as well :)
declare function directory-child-uris ($directory-uri as xs:string) as xs:string*
{
	let $dir-uri := dir-uri ($directory-uri)

	return
	for $uri in xdmp:directory ($dir-uri, "1")
	return fn:substring-after (fn:base-uri ($uri), $dir-uri)
};