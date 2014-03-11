window.spud.admin.date_picker = {}
picker = spud.admin.date_picker

picker.init = (selector) ->
  selector = selector || '.spud_form_data_picker'
  $(selector).each ->
    $this = $(this)
    $this.datepicker
      format: 'yyyy-mm-dd'
      autoclose: true

