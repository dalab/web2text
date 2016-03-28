'use strict';

let _state = () => Iron.controller().state;

Template.layout.events({

  'change .labeling-selector'(evt) {
    var label = $(evt.target).val();
    let {dataset, doc_id} = this;
    Router.go('view.page',
              {dataset_id: dataset, id: doc_id, cur_labeling: label});
  },

  'click .next-page'(evt) {
    evt.preventDefault();
    let {dataset, doc_id} = Documents.findOne(
      {'doc_id': {'$gt': this.doc_id}},
      {sort: {'doc_id':1}}
    );
    if (!doc_id) return;
    Router.go('view.page',
              {dataset_id: dataset, id: doc_id, cur_labeling: _state().get('cur_labeling')} );
  },

  'click .prev-page'(evt) {
    evt.preventDefault();
    let {dataset, doc_id} = Documents.findOne(
      {'doc_id': {'$lt': this.doc_id}},
      {sort: {'doc_id':-1}}
    );
    if (!doc_id) return;
    Router.go('view.page',
              {dataset_id: dataset, id: doc_id, cur_labeling: _state().get('cur_labeling')});
  }

});


