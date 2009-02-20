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

xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/state-lib.xqy";
import module namespace jcrfslib="http://marklogic.com/jcr/fs" at "../lib/fs-lib.xqy";

declare default element namespace "http://marklogic.com/jcr";

declare variable $state-doc-uri as xs:string external;
declare variable $workspace-root as xs:string external;
declare variable $deltas-uri as xs:string external;

declare variable $state as element(workspace) := doc ($state-doc-uri)/workspace;
declare variable $deltas as element(change-list) := doc ($deltas-uri)/change-list;
(: FIXME: move this to the state lib :)
declare variable $tx-tmp-dir as xs:string :=
	fn:concat ($workspace-root, "/", fn:string ($deltas/tx-dir), "/",
	fn:string ($deltas/tx-id), "/");

let $dummy := state:starting()
let $new-state := state:apply-state-updates ($state, $deltas, $workspace-root)

return
(
	state:gather-new-blob-uris ($new-state, $deltas),
	xdmp:document-insert ($state-doc-uri, $new-state),
	xdmp:directory-delete ($tx-tmp-dir),
	state:finished ($workspace-root)
)