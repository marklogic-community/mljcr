xquery version "1.0-ml";

declare default element namespace "http://marklogic.com/jcr";

declare variable $log-level := "debug";

declare variable $state-uri external;
(: declare variable $expression external; :)
declare variable $query external;
declare variable $dummy external;

(: let $state-uri := "'/tmp/JackRabbitRepo/workspaces/default/state.xml'" :)
(:
let $query1 := "declare namespace mljcr = 'http://marklogic.com/jcr'; fn:doc('/tmp/JackRabbitRepo/workspaces/default/state.xml')//mljcr:node/@uuid"
:)

(: let $namespace := "declare namespace mljcr = 'http://marklogic.com/jcr';" :)
(: let $expression := "//mljcr:node/@uuid" :)
(: let $query := fn:concat($namespace,"fn:doc(",$state-uri,")",$expression) :)
xdmp:eval($query)

