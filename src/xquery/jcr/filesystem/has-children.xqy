xquery version "1.0-ml";

import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "fs-lib.xqy";

declare variable $uri external;

jcrfslib:directory-child-count ($uri) gt 0