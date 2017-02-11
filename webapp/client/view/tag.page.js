'use strict';

let _state = () => Iron.controller().state;

Meteor.startup(() => {
  Session.setDefault("_altPressed",false);
  Session.setDefault("_zoomLevel",1);
});

let _getLabel = (doc_id) => {
  const label_name = Tagging.labelName();
  return Labels.findOne({label_name, doc_id});
};

let _currentStep = (doc_id) => {
  if (!Meteor.user()) return 1;
  const n       = Tagging.PAGES_PER_BATCH;
  let   plus    = 1;
  const labels  = _getLabel(doc_id);
  if (labels && labels.metadata.finished) {
    plus = 0;
  }
  const n_tagged = Meteor.user().n_tagged || 0;
  return (n_tagged + plus-1)%n+1;
};

Template.tagPage.helpers({
  'isRemoving': function () {
    return Session.get("_altPressed");
  },
  'zoomLevel': function () {
    return Session.get("_zoomLevel");
  },
  'zoomLevelHuman': function () {
    return Math.round(Session.get("_zoomLevel")*100);
  },
  'labels': function () {
    const labeling = _getLabel(this.doc_id);
    if (!labeling) return [];
    else return labeling.labels;
  },
  'saved': function () {
    const labels = _getLabel(this.doc_id);
    return labels && labels.metadata.finished;
  },
  'status': function () {
    return _currentStep(this.doc_id);
  },
  'tagTotal': function () {
    return Tagging.PAGES_PER_BATCH;
  },
  'nextIsDone': function () {
    return _currentStep(this.doc_id) == Tagging.PAGES_PER_BATCH;
  },
  'isLinux': function () {
    return navigator.platform.toUpperCase().indexOf('LINUX')!==-1;
  },
  'comments': function () {
    const labeling = _getLabel(this.doc_id);
    if (!labeling) return "";
    return labeling.metadata.comments;
  },
  'labelUpdate': function () {
    return (updates) =>
      Meteor.call('tagBlocks', this.doc_id, updates);
  },
});

Template.tagPage.events({
  'click .next-tag-page'(evt) {
    evt.preventDefault();

    const labels = _getLabel(this.doc_id);
    const done = labels && labels.metadata.finished;
    if (!done) return;

    const lastOfSeries = _currentStep(this.doc_id) == Tagging.PAGES_PER_BATCH;
    if (lastOfSeries) {
      Router.go("done.tagging");
    } else {
      Tagging.goToNextPage();
    }
  },

  'click .skip-button'(evt) {
    evt.preventDefault();

    const labels = _getLabel(this.doc_id);
    const done = labels && labels.metadata.finished;
    if (done) return;

    Meteor.call('markSkipped', this.doc_id, $(evt.target).data('reason'), (err,res) => {
      Tagging.goToNextPage();
    });

  },

  'click .save-button'(evt) {
    evt.preventDefault();
    if (Session.get('iframe-loading')) return;
    const labels = _getLabel(this.doc_id);
    const done = labels && labels.metadata.finished;
    let hasOne = false;
    labels.labels.forEach((lab) => {
      if (lab===1) hasOne = true
    });
    if (done || hasOne || confirm("Are you sure there is no content on this page at all?")) {
      Meteor.call('markDone', this.doc_id, !done);
    }
  },

  'input #zoom-level, change #zoom-level'(evt) {
    Session.set('_zoomLevel',$(evt.target).val()/100);
  },

  'click .prev-tag-page'(evt) {
    evt.preventDefault();
    let {dataset, doc_id} = Documents.findOne(
      {'doc_id': {'$lt': this.doc_id}},
      {sort: {'doc_id':-1}}
    );
    if (!page) return;
    Router.go('tag.page',
              {dataset_id: dataset, id: doc_id});
  },

  'change .comments-box'(evt, instance) {
    Meteor.call('setComments',instance.data.doc_id, $(evt.target).val());
  }
});

Template.tagPage.onRendered(function () {
  const labelsEntry = _getLabel(this.data.doc_id);
  if (!labelsEntry) {
    console.log("There are no labels yet, initialize");
    Meteor.call('setLabels',
      this.data.dataset,
      this.data.doc_id,
      new Array(this.data.n_blocks).fill(0)
    );
  }
});
