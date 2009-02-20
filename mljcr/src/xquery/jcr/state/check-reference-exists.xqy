xquery version "1.0-ml";

(:
 Copyright (c) 2009,  Mark Logic Corporation.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 The use of the Apache License does not indicate that this project is
 affiliated with the Apache Software Foundation.
:)

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $log-level := "debug";

declare variable $uri external;
declare variable $uuid external;

declare variable $state as element (workspace) := doc ($uri)/workspace;

let $result := state:check-reference-exists ($state, $uuid)

(:
let $dummy := xdmp:log (fn:concat ("check-reference-exists: uri=", $uri, ", uuid=", $uuid, ", result=", $result), $log-level)
:)

return $result