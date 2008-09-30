
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";
import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "../lib/fs-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $uri as xs:string external;
declare variable $deltas-uri external;

declare variable $save-debug-history := false();

declare variable $state as element (workspace) := doc ($uri)/workspace;
declare variable $deltas as element (change-list) := doc ($deltas-uri)/change-list;
declare variable $workspace-uri-root as xs:string := fn:substring-before ($uri, fn:concat ("/", $state:STATE_DOC_NAME));


declare function local:get-counter ($uri as xs:string) as xs:string
{
	let $prev-number as xs:integer? := xs:integer (fn:doc ($uri)/counter)
	let $number := if (fn:exists ($prev-number)) then ($prev-number + 1) else 1
	let $dummy := xdmp:document-insert ($uri, <counter>{$number}</counter>)
	return
	if ($number < 10)
	then fn:concat ("00", $number)
	else if ($number < 100)
	then fn:concat ("0", $number)
	else fn:string ($number)
};

declare function local:save-debug-history ($old-state as element(workspace),
	$new-state as element(workspace), $deltas as element(change-list))
{
	let $base-uri := fn:substring-before ($uri, $state:STATE_DOC_NAME)
	let $counter-uri := fn:concat ($base-uri, "history-counter.xml")
	let $number := local:get-counter ($counter-uri)
	let $history-uri := fn:concat ($base-uri, "state-history-", $number, ".xml")
	return xdmp:document-insert ($history-uri,
		<history uri="{$history-uri}">
			<before>{$old-state}</before>
			<pruned>{state:prune-deleted ($old-state, $deltas, $workspace-uri-root)}</pruned>
			<deltas>{$deltas}</deltas>
			<after>{$new-state}</after>
		</history>
	)
};


let $new-state as element(workspace) := state:apply-state-updates ($state, $deltas, $workspace-uri-root)
let $dummy := if ($save-debug-history) then local:save-debug-history ($state, $new-state, $deltas) else ()

return
(
	xdmp:node-replace ($state, $new-state),
	jcrfslib:delete-and-prune-dirs ($deltas-uri)
)

(:
xdmp:node-replace ($state, state:apply-state-updates ($state, $deltas))
:)
