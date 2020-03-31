!function ($) {
  "use strict"; // jshint ;_;

  var _render = function(obj, p){
    var parent = $(p);
    var ic = 0;
    var count = 0 ;
    for (var key in obj) {
      if (!obj.hasOwnProperty(key)){
        continue;
      }
      count+=1;
    }

    for (var key in obj) {
      if (!obj.hasOwnProperty(key)){
        continue;
      }
      ic +=1;
      var coma = '';
      if (ic < count){
        coma = ',';
      }
      if (obj[key] === null){
        parent.append('<li><span class="key">'+key+':</span><span class="null"> null </span>'+coma+'</li>');
      } else if (typeof obj[key] === 'boolean'){
        parent.append('<li><span class="key">'+key+':</span><span class="boolean">'+obj[key]+'</span>'+coma+'</li>');
      } else if (typeof obj[key] === 'number'){
        parent.append('<li><span class="key">'+key+':</span><span class="number">'+obj[key]+'</span>'+coma+'</li>');
      } else if (typeof obj[key] === 'string'){
        parent.append('<li><span class="key">'+key+':</span><span class="string">"'+obj[key]+'"</span>'+coma+'</li>');
      } else if ($.isArray(obj[key])) {
        var arval = $('<li><span class="key">'+key+':</span><span class="fold folded">[</span><ul class="array" style="display: none;"></ul><span>]</span>'+coma+'</li>');	// Default to folding arrays. DLS on 7/29/2015.
        parent.append(arval);
        arval.find('.unfold').data('card', _render(obj[key], arval.find('.array'))) ;
      }else{
        var oval = $('<li><span class="key">'+key+':</span><span class="fold unfolded">{</span><ul class="object"></ul><span>}</span>'+coma+'</li>');
        parent.append(oval);
        oval.find('.unfold').data('card', _render(obj[key], oval.find('.object')));
      }
    }
    return ic;
  };

  $(document).on("click", '.jsontree .fold.unfolded', function(e){
    e.preventDefault();
    $(this).removeClass('unfolded').addClass('folded').parent().find('ul').slideUp();
  });

  $(document).on('click', '.jsontree .fold.folded', function(e){
    e.preventDefault();
    $(this).removeClass('folded').addClass('unfolded').parent().find('ul').slideDown();
  });

  var JsonTree = function(self){
    var j = self.data('jsontree');
    self.empty();	// Always start from scratch. DLS on 12/4/2013.
    self.append('<ul class="jsontree"></ul>');
    _render([j], self.find('.jsontree'));
  };

  $.fn.jsontree = function (option) {
    return this.each(function () {	// Always start from scratch. DLS on 12/4/2013.
      var self = $(this);
      var type = typeof(option);
      if (type == 'string')
        self.data('jsontree', $.parseJSON(option));
      else
        self.data('jsontree', option);
      new JsonTree(self);
    });
  };

}(window.jQuery);