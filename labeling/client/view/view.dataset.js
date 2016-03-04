'use strict';

Template.viewDataset.helpers({
  pages() {
    return Documents.find( {dataset: this.id}, {sort: {doc_id: 1}} );
  },
  hostname(url) {
    var anchor = document.createElement('a');
    anchor.href = url;
    return anchor.hostname;
  },
  countForDocument(doc_id) {
    return Labels.find({doc_id, user_generated: true, "metadata.finished": true}).count();
  },
  skippedForDocument(doc_id) {
    return Labels.find({doc_id, user_generated: true, "metadata.skipped": true}).count();
  }
});

Template.viewDataset.events({
  'click .go-to-page'() {
    Router.go('view.page',{dataset_id: this.dataset, id: this.doc_id});
  }
});

Template.viewDataset.onCreated(function () {
    this.subscribe('user_label_basic_for_dataset',this.data.id);
});
