xquery version "1.0-ml";

import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "../lib/fs-lib.xqy";

declare variable $uri external;

if (jcrfslib:uri-exists ($uri))
then (jcrfslib:doc-length ($uri), 0, jcrfslib:is-directory ($uri))
else ()