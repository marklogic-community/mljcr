xquery version "1.0-ml";

declare variable $uri external;
declare variable $dir-uri := fn:concat ($uri, '/');

fn:exists (doc ($uri))
