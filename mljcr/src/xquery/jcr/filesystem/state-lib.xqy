
xquery version "1.0-ml";

module namespace jcrstatelib="http://marklogic.com/jcr/state";

declare private function handle-deleted-states ($state as element(workspace),
	$deltas as element(jcr-change-list))
{
	for $node in $deltas/deleted-states/node
	let $id := fn:string ($node)
let $dummy := xdmp:log (fn:concat ("deleted ", $id))
	return xdmp:node-delete ($state//node[@uuid = $id])
};

(: =============================================================== :)

declare private function find-node-name ($deltas as element(jcr-change-list),
	$id as xs:string)
	as xs:string
{
	fn:string ($deltas//node/nodes/node[@uuid = $id]/@name)
};

declare private function build-added-nodes ($deltas as element(jcr-change-list),
	$id as xs:string)
	as element (node)*
{
let $dummy := xdmp:log (fn:concat ("build: id=", $id))
	let $added := $deltas/added-states
	let $node := $added/node[@uuid = $id]
	return
	<node>
		{attribute { "name" } { find-node-name ($deltas, $id) }}
		{$node/@*}
		{$node/mixinTypes}
{$node/nodes}
		{$added/property[@parentUUID = $id]}
		{
			for $node in $added/node[@parentUUID = $id]
			return
			build-added-nodes ($deltas, fn:string ($node/@uuid))
		}
	</node>
};

declare private function top-level-added-nodes ($state as element(workspace),
	$deltas as element(jcr-change-list))
{
	let $added := $deltas/added-states
	for $node in $added/node
	let $my-id := fn:string ($node/@uuid)
	let $my-parent-id := fn:string ($node/@parentUUID)
let $dummy := xdmp:log (fn:concat ("scanning node: id=", $my-id, ", parent=", $my-parent-id))
	where fn:empty ($added/node[fn:string (@uuid) = $my-parent-id])
	return
	build-added-nodes ($deltas, $my-id)
};

declare private function handle-added-states ($state as element(workspace),
	$deltas as element(jcr-change-list))
{
	let $roots := top-level-added-nodes ($state, $deltas)
let $dummy := xdmp:log (fn:concat ("roots=", fn:count ($roots)))

	for $node in $roots
	let $parent-id := fn:string ($node/@parentUUID)
	let $parent := $state/node[fn:string (@uuid) = $parent-id]
	return
	if (fn:exists ($parent))
	then xdmp:node-insert-child ($parent, $node)
	else xdmp:node-insert-child ($state, $node)

};

(: =============================================================== :)

declare private function update-node ($old-node as element(node), $new-node as element(node))
{
	if (fn:exists ($old-node/nodes) and
		fn:not (fn:deep-equal ($old-node/nodes, $new-node/nodes)))
	then xdmp:node-replace ($old-node/nodes, $new-node/nodes)
	else xdmp:node-insert-child ($old-node, $new-node/nodes)
	,
	if (fn:exists ($old-node/mixinTypes) and
		fn:not (fn:deep-equal ($old-node/mixinTypes, $new-node/mixinTypes)))
	then xdmp:node-replace ($old-node/mixinTypes, $new-node/mixinTypes)
	else xdmp:node-insert-child ($old-node, $new-node/mixinTypes)

	(: TODO: update attributes :)
	(: TODO: Do full node replace? :)
};

declare private function handle-modified-states ($state as element(workspace),
	$deltas as element(jcr-change-list))
{
	for $node in $deltas/modified-states/node
	let $old-node := $state//node[@uuid = $node/@uuid]
	return update-node ($old-node, $node)
};

(: =============================================================== :)

declare private function handle-modified-refs ($state as element(workspace),
	$deltas as element(jcr-change-list))
{
};

(: =============================================================== :)
(: =============================================================== :)

declare function handle-state-updates ($state as element(workspace),
	$deltas as element(jcr-change-list))
{
	handle-deleted-states ($state, $deltas),
	handle-added-states ($state, $deltas),
	handle-modified-states ($state, $deltas),
	handle-modified-refs ($state, $deltas)
};

(: =============================================================== :)

declare function check-node-exists ($state as element(workspace), $id as xs:string)
{
	fn:exists ($state/node[fn:string(@uuid) = $id])
};

declare function query-node-state ($state as element(workspace), $id as xs:string)
	as element(node)?
{
	let $node := $state//node[fn:string(@uuid) = $id]

	return
	if (fn:empty ($node))
	then ()
	else
	<node>
		{$node/@*}
		{$node/mixinTypes}
		{$node/nodes}
		<properties>{
			for $prop in $node/property
			return
			<property>{$prop/@name}</property>
		}</properties>
	</node>
};

(: =============================================================== :)

declare function check-property-exists ($state as element(workspace),
	$id as xs:string, $name as xs:string)
{
	fn:exists ($state/node[fn:string(@uuid) = $id]/property[fn:string(@name) = $name])
};


declare function query-property-state ($state as element(workspace),
	$id as xs:string, $name as xs:string)
	as element(property)?
{
	$state/node[fn:string(@uuid) = $id]/property[fn:string(@name) = $name]
};

(: =============================================================== :)

