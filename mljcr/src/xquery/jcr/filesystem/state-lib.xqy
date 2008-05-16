
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

(: True is a property is on the delete list :)
declare private function property-deleted ($prop as element(property),
	$deltas as element(jcr-change-list))
	as xs:boolean
{
	fn:exists ($deltas/deleted-states/property[@parrentUUID = $prop/@parentUUID][@name = $prop/@name])
};

(: True is a property is on the modified list :)
declare private function property-modified ($prop as element(property),
	$deltas as element(jcr-change-list))
	as xs:boolean
{
	fn:exists ($deltas/modified-states/property[@parrentUUID = $prop/@parentUUID][@name = $prop/@name])
};

(: Delete a property.  Delete the blob file for Binary properties,
   otherwise do nothing.
 :)
declare private function delete-property ($prop as element(property))
	as empty-sequence()
{
	(: delete blob if type = Binary :)
	()
};


(: Process the properties for a node, applying deltas as appropriate :)
declare private function process-properties ($node as element(node),
	$deltas as element(jcr-change-list))
	as element(property)*
{
	for $prop in $node/property
	return
	if (property-deleted ($prop, $deltas))
	then delete-property ($prop)
	else $prop
	,
	$deltas/added-states/property[@parentUUID = $node/@uuid]
};

declare private function add-new-nodes ($nodes as element(node)*,
	$deltas as element(jcr-change-list))
	as element(node)*
{
let $dummy := xdmp:log (fn:concat ("add-new-nodes=", fn:count ($nodes)))
	let $added := $deltas/added-states

	for $node in $nodes
	let $id := fn:string ($node/@uuid)
	return
	<node>
		{attribute { "name" } { find-node-name ($deltas, $id) }}
		{$node/@*}
		{$node/mixinTypes}
		{$added/property[@parentUUID = $id]}
{$node/nodes}
		{add-new-nodes ($added/node[@parentUUID = $id], $deltas)}
	</node>

};

(: Produces one node element, either copying of building a new one by
   applying the deltas
 :)
declare private function process-one-node ($n as element(node),
	$deltas as element(jcr-change-list))
	as element(node)
{
let $dummy := xdmp:log (fn:concat ("process-one-node=", fn:string ($n/@uuid)))
	let $replace-node := $deltas/modified-states/node[@uuid = $n/@uuid]
	let $node := if ($replace-node) then $replace-node else $n
	return
	<node>{
		$node/@*,
		$node/mixinTypes,
		process-nodes ($n/node, $deltas),
		add-new-nodes ($deltas/added-states/node[@parentUUID = $node/uuid], $deltas),
		process-properties ($node, $deltas)
	}</node>
};

(: Iterates over a sequence of node elements and makes the changes
   that apply to each.
 :)
declare private function process-nodes ($nodes as element(node)*,
	$deltas as element(jcr-change-list))
	as element(node)*
{
let $dummy := xdmp:log (fn:concat ("process-nodes=", fn:count ($nodes)))
	for $node in $nodes
	return
	if ($deltas/deleted-states/node[@uuid] = $node/@uuid)
	then ()
	else process-one-node ($node, $deltas)
};

(: Returns a new workspace node with deltas applied :)
declare function apply-state-updates ($state as element(workspace),
	$deltas as element(jcr-change-list))
	as element(workspace)
{
let $dummy := xdmp:log (fn:concat ("apply-state-updates=", xdmp:quote ($state)))
return
	<workspace>{
		process-nodes ($state/node, $deltas),
		add-new-nodes ($deltas/added-states/node[@parentUUID = ""], $deltas)
	}</workspace>
};


(:
declare function handle-state-updates ($state as element(workspace),
	$deltas as element(jcr-change-list))
{

	handle-deleted-states ($state, $deltas),
	handle-added-states ($state, $deltas),
	handle-modified-states ($state, $deltas),
	handle-modified-refs ($state, $deltas)
};
:)

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

