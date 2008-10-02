xquery version "1.0-ml";

module namespace jcrfslib="http://marklogic.com/jcr/fs";


(: TODO: look at properties for pre-existing length property :)
declare function doc-length ($uri as xs:string) as xs:integer
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
	else fn:count (directory-child-uris ($uri))
};

declare function directory-file-uris ($directory-uri as xs:string) as xs:string*
{
	let $dir-uri := dir-uri ($directory-uri)

	return
	for $uri in xdmp:directory ($dir-uri, "1")
	return fn:substring-after (fn:base-uri ($uri), $dir-uri)
};

declare function directory-dir-uris ($uri as xs:string) as xs:string*
{
	let $dir := dir-uri ($uri)
	for $i in xdmp:directory-properties ($dir)//prop:directory
	return fn:substring-before (fn:substring-after (fn:base-uri ($i), $dir), "/")
};

declare function directory-child-uris ($uri as xs:string) as xs:string*
{
	directory-dir-uris ($uri), directory-file-uris ($uri)
};

(: ==================================================================== :)

declare private function parent-dir-uri ($uri as xs:string) as xs:string?
{
	let $parent-uri := fn:replace ($uri, "/[^/]*/?$", "/")
	let $foo := fn:replace ($parent-uri, "[^/]", "")
	return
	if ((fn:empty ($foo)) or (fn:string-length ($foo) le 1))
	then ()
	else $parent-uri
};

declare function prune-dir ($uri as xs:string, $max-children) as empty-sequence()
{
	let $my-parent-uri := parent-dir-uri ($uri)
	let $parent-count := if (fn:empty ($my-parent-uri)) then ($max-children + 1) else directory-child-count ($my-parent-uri)
	return
	if ($parent-count le $max-children)
	then prune-dir ($my-parent-uri, 1)
	else xdmp:directory-delete ($uri)
};

declare function delete-and-prune-dirs ($uri as xs:string) as empty-sequence()
{
	let $parent-dir-uri := parent-dir-uri ($uri)
	return
	if (directory-child-count ($parent-dir-uri) gt 1)
	then xdmp:document-delete ($uri)
	else prune-dir ($parent-dir-uri, 1)
};
