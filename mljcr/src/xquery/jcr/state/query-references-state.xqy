
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $log-level := "debug";

declare variable $uri external;
declare variable $uuid external;

declare variable $state as element (workspace) := doc ($uri)/workspace;

let $result := state:query-references-state ($state, $uuid)
let $dummy := xdmp:log (fn:concat ("query-references-state: uri=", $uri, ", uuid=", $uuid, ", result=", if (fn:empty ($result)) then "(empty)" else xdmp:quote ($result)), $log-level)

return $result