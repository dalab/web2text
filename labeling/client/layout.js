'use strict';

let _state = () => Iron.controller().state;

Template.layout.events({

  'change .labeling-selector'(evt) {
    var label = $(evt.target).val();
    let {group, doc_id} = this;
    Router.go('view.page',
              {dataset_id: group, id: doc_id, cur_labeling: label});
  },

  'click .next-page'(evt) {
    evt.preventDefault();
    let {group, doc_id} = Pages.findOne(
      {'doc_id': {'$gt': this.doc_id}},
      {sort: {'doc_id':1}}
    );
    if (!page) return;
    Router.go('view.page',
              {dataset_id: group, id: doc_id, cur_labeling: _state().get('cur_labeling')} );
  },

  'click .prev-page'(evt) {
    evt.preventDefault();
    let {group, doc_id} = Pages.findOne(
      {'doc_id': {'$lt': this.doc_id}},
      {sort: {'doc_id':-1}}
    );
    if (!page) return;
    Router.go('view.page',
              {dataset_id: group, id: doc_id, cur_labeling: _state().get('cur_labeling')});
  }

});


