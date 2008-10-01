
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";
import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "../lib/fs-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $state-doc-uri as xs:string external;
declare variable $workspace-root as xs:string external;
declare variable $deltas-uri as xs:string external;

declare variable $state as element(workspace) := doc ($state-doc-uri)/workspace;
declare variable $deltas as element(change-list) := doc ($deltas-uri)/change-list;

let $new-state := state:apply-state-updates ($state, $deltas, $workspace-root)

return
(
	state:find-new-blob-uris ($new-state, $deltas),
	xdmp:node-replace ($state, $new-state),
	jcrfslib:delete-and-prune-dirs ($deltas-uri)
)