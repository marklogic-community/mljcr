
xquery version "1.0-ml";

declare variable $uri-default := "/tmp/JackRabbitRepo/workspaces/test/state.xml";
declare variable $uri-version := "/tmp/JackRabbitRepo/version/state.xml";

(:
xdmp:document-insert ($uri, <workspace/>)
:)


(:
for $i in collection()
return base-uri ($i)
:)


xdmp:set-response-content-type ("text/xml"),
fn:doc ($uri-default)


