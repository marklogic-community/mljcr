
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $uri external;
declare variable $uuid external;
declare variable $name external;

declare variable $decoded-name := xdmp:url-decode ($name);
declare variable $state as element (workspace) := doc ($uri)/workspace;

let $result := state:check-property-exists ($state, $uuid, $decoded-name)
(:
let $dummy := xdmp:log (fn:concat ("check-property-exists: uri=", $uri, ", uuid=", $uuid, ", name=", $decoded-name, ", result=", $result))
:)

return $result