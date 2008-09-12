xquery version "1.0-ml";

module namespace jcrstatelib="http://marklogic.com/jcr/state";

declare private function find-node-name ($deltas as element(jcr-change-list),
	$id as xs:string)
	as xs:string
{
	fn:string ($deltas//node/nodes/node[@uuid = $id]/@name)
};

(: True if a property is on the delete list :)
declare private function property-deleted ($prop as element(property),
	$deltas as element(jcr-change-list))
	as xs:boolean
{
	fn:exists ($deltas/deleted-states/property[@parrentUUID = $prop/@parentUUID][@name = $prop/@name])
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
declare private function process-one-node ($node as element(node),
	$deltas as element(jcr-change-list))
	as element(node)
{
let $dummy := xdmp:log (fn:concat ("process-one-node=", fn:count ($node)))
	let $replace-node := $deltas/modified-states/node[@uuid = $node/@uuid]
	return
	<node>{
		if ($replace-node)
		then process-one-node ($replace-node, $deltas)
		else
		(
			$node/@*,
			$node/mixinTypes,
			process-nodes ($node/node, $deltas),
			add-new-nodes ($deltas/added-states/node[@parentUUID = $node/uuid], $deltas),
			process-properties ($node, $deltas)
		)
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
		add-new-nodes ($deltas/added-states/node[@parentUUID = ""], $deltas),
		process-nodes ($state/node, $deltas)
	}</workspace>
};
