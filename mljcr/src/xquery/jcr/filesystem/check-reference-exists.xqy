
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "state-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $uri external;
declare variable $uuid external;

declare variable $state as element (workspace) := doc ($uri)/workspace;

state:check-reference-exists ($state, $uuid)
