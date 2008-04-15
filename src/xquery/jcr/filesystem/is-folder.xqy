xquery version "1.0-ml";

declare variable $uri external;
declare variable $dir-uri := fn:concat ($uri, '/');

fn:exists (xdmp:document-properties ($uri)//prop:directory) or
fn:exists (xdmp:document-properties ($dir-uri)//prop:directory)
