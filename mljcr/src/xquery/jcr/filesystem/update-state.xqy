
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "state-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $uri external;
declare variable $deltas-str external;

declare variable $state as element (workspace) := doc ($uri)/workspace;
declare variable $deltas as element (change-list) := xdmp:unquote ($deltas-str)/element();


xdmp:log (fn:concat ("uri: ", $uri, ", type: ", xdmp:node-kind ($deltas))),

xdmp:node-replace ($state, state:apply-state-updates ($state, $deltas))
