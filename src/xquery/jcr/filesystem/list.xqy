xquery version "1.0-ml";

import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "fs-lib.xqy";

(:
declare variable $uri external;
:)
declare variable $uri := "/JackRabbitRepo/repository/meta";

jcrfslib:directory-child-uris ($uri)

