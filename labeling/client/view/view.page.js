'use strict';

let _state = () => Iron.controller().state;

Template.viewPage.helpers({
  dataset() {
    return Datasets.findOne({id: this.dataset});
  },
  availableLabelings() {
    return Labels.find({doc_id:this.doc_id}).fetch().map(x => x.label_name);
  },
  labelingSelected(lab) {
    let curr = _state().get('cur_labeling');
    if(lab == curr) return "selected";
    else return "";
  }
});

Template.viewPage.events({
  'click .view-alignment'() {
    Modal.show( 'viewAlignmentModal', this, {keyboard: true} )
  }
});


Template.viewPage.rendered = function() {

  let iframe = document.getElementById("page");
  let idoc = iframe.contentDocument;
  idoc.write(this.data.blocked_source);

  this.autorun(() => {
    let data = Router.current().data();
    let label_name = Iron.controller().state.get('cur_labeling');
    if (!label_name) return;
    PageBlocks.markBlocks(idoc, Labels.findOne({label_name, doc_id: data.doc_id}).labels);
  });

  const _zoomStyleNode = PageBlocks.addStyleString(idoc,PageBlocks.zoomStyle(1));
  this.autorun(() => {
    console.log("set zoom level");
    _zoomStyleNode.innerHTML = PageBlocks.zoomStyle(Session.get('_zoomLevel'));
  });

  PageBlocks.deactivateLinks(idoc);

  PageBlocks.addStyleString(idoc, PageBlocks.pageCss);

};