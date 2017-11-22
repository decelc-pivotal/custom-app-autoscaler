var rules_basic = {
  condition: 'AND',
  rules: [{
    id: 'name',
    operator: 'equal',
    value: 'adfadf'
  }]
};

var filters_init = [{
	    id: 'id',
	    label: 'Identifier',
	    type: 'string',
	    placeholder: '____-____-____',
	    operators: ['equal', 'not_equal'],
	    validation: {
	      format: /^.{4}-.{4}-.{4}$/
	    }
	  }];

var rules_filters = [{
    id: 'name',
    label: 'Name',
    type: 'string'
  }, {
    id: 'category',
    label: 'Daily Market &#916;',
    type: 'double',
    validation: {
        min: 0,
        step: 0.01
      }
  }, {
    id: 'in_stock',
    label: 'In stock',
    type: 'integer',
    input: 'radio',
    values: {
      1: 'Yes',
      0: 'No'
    },
    operators: ['equal']
  }, {
    id: 'price',
    label: 'Price',
    type: 'double',
    validation: {
      min: 0,
      step: 0.01
    }
  }];


$('#builder-basic').queryBuilder({
  plugins: ['bt-tooltip-errors'],
  
  filters: filters_init
});

$('#btn-reset').on('click', function() {
  $('#builder-basic').queryBuilder('reset');
});

$('#btn-set').on('click', function() {
  $('#builder-basic').queryBuilder('setRules', rules_basic);
});

$('#btn-get').on('click', function() {
  var result = $('#builder-basic').queryBuilder('getRules');
  
  if (!$.isEmptyObject(result)) {
    alert(JSON.stringify(result, null, 2));
  }
});