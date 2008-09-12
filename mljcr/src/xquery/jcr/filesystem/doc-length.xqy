xquery version "1.0-ml";

import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "../lib/fs-lib.xqy";

declare variable $uri external;

jcrfslib:doc-length ($uri)
