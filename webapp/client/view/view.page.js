'use strict';

let _state = () => Iron.controller().state;

Template.viewPage.helpers({
  dataset() {
    return Datasets.findOne({id: this.dataset});
  },
  availableLabelings() {
    return Labels.find({doc_id:this.doc_id,$or: [{"metadata.finished": true},{user_generated:false}]}).fetch().map(x => x.label_name);
  },
  curLabeling() {
    const curr = _state().get('cur_labeling');
    return Labels.findOne({label_name: curr, doc_id: this.doc_id});
  },
  labels() {
    const curr = _state().get('cur_labeling');
    const labeling = Labels.findOne({label_name: curr, doc_id: this.doc_id});
    if (!labeling) return [];
    else return labeling.labels;
  },
  curLabelUser() {
    const curr = _state().get('cur_labeling');
    const user = curr.substring(5,curr.length);
    Meteor.subscribe("userName",user);
    return Meteor.users.findOne({_id:user});
  },
  labelingSelected(lab) {
    const curr = _state().get('cur_labeling');
    if(lab == curr) return "selected";
    else return "";
  },
  zoomLevelHuman: function () {
    return Math.round(Session.get("_zoomLevel")*100);
  },
  zoomLevel: function () {
    return Session.get("_zoomLevel");
  },
  hostname(url) {
    var anchor = document.createElement('a');
    anchor.href = url;
    return anchor.hostname;
  }
});

Template.viewPage.events({
  'click .view-alignment'() {
    const curr = _state().get('cur_labeling');
    Modal.show( 'viewAlignmentModal', {doc_id: this.doc_id, label_name: curr}, {keyboard: true} )
  },

  'input #zoom-level, change #zoom-level'(evt) {
    Session.set('_zoomLevel',$(evt.target).val()/100);
  },

  'click .remove-page'(evt) {
    evt.preventDefault();
    Meteor.call("removeDocument", this.doc_id, true);
  },
  'click .unremove-page'(evt) {
    evt.preventDefault();
    Meteor.call("removeDocument", this.doc_id, false);
  }
});
