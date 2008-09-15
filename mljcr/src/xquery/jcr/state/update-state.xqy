
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $uri external;
declare variable $deltas-str external;

declare variable $state as element (workspace) := doc ($uri)/workspace;
declare variable $deltas as element (change-list) := xdmp:unquote ($deltas-str)/element();


(:
xdmp:log (fn:concat ("apply-state-updates: uri: ", $uri)),
xdmp:log ("======================= deltas ==========================="),
xdmp:log ($deltas-str),
xdmp:log ("======================= before ==========================="),
xdmp:log (xdmp:quote ($state)),
xdmp:log ("======================= after ============================"),
xdmp:log (xdmp:quote (state:apply-state-updates ($state, $deltas))),
xdmp:log ("=========================================================="),
xdmp:log ("=========================================================="),
:)

xdmp:node-replace ($state, state:apply-state-updates ($state, $deltas))
