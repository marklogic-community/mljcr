
xquery version "1.0-ml";

declare function local:mult ($x as xs:decimal, $y as xs:decimal) as xs:string
{
    fn:concat ($x, " x ", $y, " = ", $x * $y)
};

local:mult ((1, 2, 3), (5, 10))