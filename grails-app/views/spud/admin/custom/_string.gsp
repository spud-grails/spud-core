<label for="${objectType}.${objectField}.${customField.name}" class="control-label col-sm-2">${customField.label}</label>
<div class="col-sm-8">
  <g:textField name="${objectType}.${objectField}.${customField.name}" value="${object ? object[objectField] ? object[objectField][customField.name] : null : null}" class="form-control" maxlength="250" />
</div>