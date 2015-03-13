'use strict';
angular.module('notifyjs', [])
  .provider('notifyjs', function notifyjsProvider() {

    // this.defaults = {
    //   clickToHide: true,
    //   autoHide: true,
    //   autoHideDelay: 5000,
    //   arrowShow: true,
    //   arrowSize: 5,
    //   breakNewLines: true,
    //   elementPosition: 'bottom',
    //   globalPosition: 'top right',
    //   style: 'bootstrap',
    //   className: 'error',
    //   showAnimation: 'slideDown',
    //   showDuration: 400,
    //   hideAnimation: 'slideUp',
    //   hideDuration: 200,
    //   gap: 5
    // }

    this.defaults = {};

    this.$get = function notifyjsService() {

      var notifyjs = $.notify;
      notifyjs.defaults(this.defaults);

      return notifyjs;
    };
  });