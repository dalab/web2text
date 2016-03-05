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
  'iframeLoading': function () {
    return Session.get('iframe-loading')
  },
  zoomLevel: function () {
    return Math.round(Session.get("_zoomLevel")*100);
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
    console.log('thiny has changed');
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


Template.viewPage.rendered = function() {

  Session.set('iframe-loading',false) //disabled;
  let iframe = document.getElementById("page");
  let idoc = iframe.contentDocument;
  idoc.open();
  idoc.write(this.data.blocked_source);
  idoc.close();
  iframe.onload = () => Session.set('iframe-loading',false);

  this.autorun(() => {
    let data = Router.current().data();
    let label_name = Iron.controller().state.get('cur_labeling');
    if (!label_name) return;
    const labeling = Labels.findOne({label_name, doc_id: data.doc_id});
    if (!labeling) return;
    PageBlocks.markBlocks(idoc, labeling.labels);
  });

  const _zoomStyleNode = PageBlocks.addStyleString(idoc,PageBlocks.zoomStyle(1));
  this.autorun(() => {
    _zoomStyleNode.innerHTML = PageBlocks.zoomStyle(Session.get('_zoomLevel'));
  });

  PageBlocks.deactivateLinks(idoc);
  PageBlocks.unblockStyles(idoc);
  PageBlocks.addStyleString(idoc, PageBlocks.pageCss);

};