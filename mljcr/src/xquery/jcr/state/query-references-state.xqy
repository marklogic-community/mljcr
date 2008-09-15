
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $uri external;
declare variable $uuid external;

declare variable $state as element (workspace) := doc ($uri)/workspace;

let $result := state:query-references-state ($state, $uuid)
(:
let $dummy := xdmp:log (fn:concat ("query-references-state: uri=", $uri, ", uuid=", $uuid, ", result=", xdmp:quote ($result)))
:)

return $result