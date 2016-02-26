'use strict';

Template.viewDataset.helpers({
  pages() {
    return Documents.find( {dataset: this.id}, {sort: {doc_id: 1}} );
  },
  hostname(url) {
    var anchor = document.createElement('a');
    anchor.href = url;
    return anchor.hostname;
  }
});

Template.viewDataset.events({
  'click .go-to-page'() {
    Router.go('view.page',{dataset_id: this.dataset, id: this.doc_id});
  }
});