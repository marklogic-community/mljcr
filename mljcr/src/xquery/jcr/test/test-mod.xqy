
xquery version "1.0-ml";

import module namespace state="http://marklogic.com/jcr/state" at "../lib/test-lib.xqy";

declare variable $uri := "/private/tmp/JackRabbitRepo/workspaces/default/state.xml";

declare variable $deltas-str1 :=
'<jcr-change-list>
  <deleted-states>
  </deleted-states>
  <added-states>
    <property name="{http://www.jcp.org/jcr/1.0}mimeType" parentUUID="5a230a56-770b-42f8-aa9f-ffc31eda47ff" multiValued="false" definitionId="1582077365" modCount="0" type="String">
      <values>
	<value>application/octet-stream</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="5a230a56-770b-42f8-aa9f-ffc31eda47ff" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{http://www.jcp.org/jcr/nt/1.0}resource</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}uuid" parentUUID="5a230a56-770b-42f8-aa9f-ffc31eda47ff" multiValued="false" definitionId="1377012443" modCount="0" type="String">
      <values>
	<value>5a230a56-770b-42f8-aa9f-ffc31eda47ff</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}data" parentUUID="5a230a56-770b-42f8-aa9f-ffc31eda47ff" multiValued="false" definitionId="1005895570" modCount="0" type="Binary">
      <values>
	<value>/5a230a56-770b-42f8-aa9f-ffc31eda47ff/%7bhttp%3a%2f%2fwww.jcp.org%2fjcr%2f1.0%7ddata.blob</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}lastModified" parentUUID="5a230a56-770b-42f8-aa9f-ffc31eda47ff" multiValued="false" definitionId="1141884970" modCount="0" type="Date">
      <values>
	<value>2005-12-19T20:57:35.000-08:00</value>
      </values>
    </property>
    <node uuid="5a230a56-770b-42f8-aa9f-ffc31eda47ff" parentUUID="e832bf6f-4964-452d-b370-feeaef0eaef3" definitionId="757098670" modCount="0" nodeType="{http://www.jcp.org/jcr/nt/1.0}resource">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
	<property name="{http://www.jcp.org/jcr/1.0}data"></property>
	<property name="{http://www.jcp.org/jcr/1.0}uuid"></property>
	<property name="{http://www.jcp.org/jcr/1.0}mimeType"></property>
	<property name="{http://www.jcp.org/jcr/1.0}lastModified"></property>
      </properties>
      <nodes></nodes>
    </node>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="e832bf6f-4964-452d-b370-feeaef0eaef3" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{http://www.jcp.org/jcr/nt/1.0}file</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}created" parentUUID="e832bf6f-4964-452d-b370-feeaef0eaef3" multiValued="false" definitionId="1162436058" modCount="0" type="Date">
      <values>
	<value>2008-05-12T14:54:02.079-07:00</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="ae6ea7e6-94b0-4f0d-b949-4d21adc342db" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{http://www.jcp.org/jcr/nt/1.0}folder</value>
      </values>
    </property>
    <node uuid="e832bf6f-4964-452d-b370-feeaef0eaef3" parentUUID="ae6ea7e6-94b0-4f0d-b949-4d21adc342db" definitionId="-1035233391" modCount="0" nodeType="{http://www.jcp.org/jcr/nt/1.0}file">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}created"></property>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{http://www.jcp.org/jcr/1.0}content" uuid="5a230a56-770b-42f8-aa9f-ffc31eda47ff"></node>
      </nodes>
    </node>
    <property name="{http://www.jcp.org/jcr/1.0}created" parentUUID="ae6ea7e6-94b0-4f0d-b949-4d21adc342db" multiValued="false" definitionId="1162436058" modCount="0" type="Date">
      <values>
	<value>2008-05-12T14:54:02.078-07:00</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="f53dc59c-e786-4d8d-bfd9-60f3afd98ef9" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{http://www.jcp.org/jcr/nt/1.0}folder</value>
      </values>
    </property>
    <node uuid="ae6ea7e6-94b0-4f0d-b949-4d21adc342db" parentUUID="f53dc59c-e786-4d8d-bfd9-60f3afd98ef9" definitionId="-1035233391" modCount="0" nodeType="{http://www.jcp.org/jcr/nt/1.0}folder">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}created"></property>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{}Request_for_GAMS_User_Account.rtf" uuid="e832bf6f-4964-452d-b370-feeaef0eaef3"></node>
      </nodes>
    </node>
    <property name="{http://www.jcp.org/jcr/1.0}created" parentUUID="f53dc59c-e786-4d8d-bfd9-60f3afd98ef9" multiValued="false" definitionId="1162436058" modCount="0" type="Date">
      <values>
	<value>2008-05-12T14:54:02.078-07:00</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}created" parentUUID="ee897460-5f1c-45a9-a5b1-576f4af204f0" multiValued="false" definitionId="1162436058" modCount="0" type="Date">
      <values>
	<value>2008-05-12T14:54:02.078-07:00</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="ee897460-5f1c-45a9-a5b1-576f4af204f0" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{http://www.jcp.org/jcr/nt/1.0}folder</value>
      </values>
    </property>
    <node uuid="f53dc59c-e786-4d8d-bfd9-60f3afd98ef9" parentUUID="ee897460-5f1c-45a9-a5b1-576f4af204f0" definitionId="-1035233391" modCount="0" nodeType="{http://www.jcp.org/jcr/nt/1.0}folder">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}created"></property>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{}rtf" uuid="ae6ea7e6-94b0-4f0d-b949-4d21adc342db"></node>
      </nodes>
    </node>
    <node uuid="ee897460-5f1c-45a9-a5b1-576f4af204f0" parentUUID="e0aeb27d-9054-4d10-8e12-9c9fce5f94f0" definitionId="-1035233391" modCount="0" nodeType="{http://www.jcp.org/jcr/nt/1.0}folder">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}created"></property>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{}www" uuid="f53dc59c-e786-4d8d-bfd9-60f3afd98ef9"></node>
      </nodes>
    </node>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="e0aeb27d-9054-4d10-8e12-9c9fce5f94f0" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{http://www.jcp.org/jcr/nt/1.0}folder</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}created" parentUUID="e0aeb27d-9054-4d10-8e12-9c9fce5f94f0" multiValued="false" definitionId="1162436058" modCount="0" type="Date">
      <values>
	<value>2008-05-12T14:54:02.077-07:00</value>
      </values>
    </property>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="53a81dc0-c90e-4bc5-ab69-a7124b0bd4a0" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{http://www.jcp.org/jcr/nt/1.0}folder</value>
      </values>
    </property>
    <node uuid="e0aeb27d-9054-4d10-8e12-9c9fce5f94f0" parentUUID="53a81dc0-c90e-4bc5-ab69-a7124b0bd4a0" definitionId="-1035233391" modCount="0" nodeType="{http://www.jcp.org/jcr/nt/1.0}folder">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}created"></property>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{}arc" uuid="ee897460-5f1c-45a9-a5b1-576f4af204f0"></node>
      </nodes>
    </node>
    <property name="{http://www.jcp.org/jcr/1.0}created" parentUUID="53a81dc0-c90e-4bc5-ab69-a7124b0bd4a0" multiValued="false" definitionId="1162436058" modCount="0" type="Date">
      <values>
	<value>2008-05-12T14:54:02.075-07:00</value>
      </values>
    </property>
    <node uuid="53a81dc0-c90e-4bc5-ab69-a7124b0bd4a0" parentUUID="cafebabe-cafe-babe-cafe-babecafebabe" definitionId="-1603354723" modCount="0" nodeType="{http://www.jcp.org/jcr/nt/1.0}folder">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}created"></property>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{}gov" uuid="e0aeb27d-9054-4d10-8e12-9c9fce5f94f0"></node>
      </nodes>
    </node>
  </added-states>
  <modified-states>
    <node uuid="cafebabe-cafe-babe-cafe-babecafebabe" parentUUID="" definitionId="-1537436024" modCount="3" nodeType="{internal}root">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{http://www.jcp.org/jcr/1.0}system" uuid="deadbeef-cafe-babe-cafe-babecafebabe"></node>
	<node name="{}gov" uuid="8ccebb8a-7f46-4d2e-ae9d-8eaea9cfda9d"></node>
	<node name="{}org" uuid="109c0c47-c768-4387-8c4e-2fcaae2555d3"></node>
	<node name="{}au" uuid="53a81dc0-c90e-4bc5-ab69-a7124b0bd4a0"></node>
      </nodes>
    </node>
  </modified-states>
  <modified-refs></modified-refs>
</jcr-change-list>
';

declare variable $deltas-create :=
'<jcr-change-list>
  <deleted-states></deleted-states>
  <added-states>
    <node uuid="cafebabe-cafe-babe-cafe-babecafebabe" parentUUID="" definitionId="-1537436024" modCount="0" nodeType="{internal}root">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{http://www.jcp.org/jcr/1.0}system" uuid="deadbeef-cafe-babe-cafe-babecafebabe"></node>
      </nodes>
    </node>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="cafebabe-cafe-babe-cafe-babecafebabe" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{internal}root</value>
      </values>
    </property>
    <node uuid="deadbeef-cafe-babe-cafe-babecafebabe" parentUUID="cafebabe-cafe-babe-cafe-babecafebabe" definitionId="-1971945898" modCount="0" nodeType="{internal}system">
      <mixinTypes></mixinTypes>
      <properties>
	<property name="{http://www.jcp.org/jcr/1.0}primaryType"></property>
      </properties>
      <nodes>
	<node name="{http://www.jcp.org/jcr/1.0}versionStorage" uuid="deadbeef-face-babe-cafe-babecafebabe"></node>
	<node name="{http://www.jcp.org/jcr/1.0}nodeTypes" uuid="deadbeef-cafe-cafe-cafe-babecafebabe"></node>
      </nodes>
    </node>
    <property name="{http://www.jcp.org/jcr/1.0}primaryType" parentUUID="deadbeef-cafe-babe-cafe-babecafebabe" multiValued="false" definitionId="1266667140" modCount="0" type="Name">
      <values>
	<value>{internal}system</value>
      </values>
    </property>
  </added-states>
  <modified-states></modified-states>
  <modified-refs></modified-refs>
</jcr-change-list>
';

declare variable $deltas := xdmp:unquote ($deltas-create)/jcr-change-list;
declare variable $deltas2 := xdmp:unquote ($deltas-str1)/jcr-change-list;


let $ws := state:apply-state-updates (<workspace/>, $deltas)
return
state:apply-state-updates ($ws, $deltas2)


