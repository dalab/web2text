'use strict';

Template.view.helpers({
  datasets: Datasets.find()
});

Template.view.events({
  'click a'() {
    Router.go('view.dataset',{id:this.id})
  }
});
