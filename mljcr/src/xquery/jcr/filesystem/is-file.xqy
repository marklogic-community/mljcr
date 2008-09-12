xquery version "1.0-ml";

import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "../lib/fs-lib.xqy";

declare variable $uri external;
declare variable $dir-uri := fn:concat ($uri, '/');

fn:exists (doc ($uri)) and fn:not (jcrfslib:is-directory ($uri))
