<g:each var="customField" in="${customFields}">
	<div class="form-group">
		<g:render template="/spud/admin/custom/${customField.type}" model="[objectField: objectField,objectType: objectType,object: object, customField:customField]"/>
	</div>
</g:each>