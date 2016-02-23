'use strict';

let _state = () => Iron.controller().state;

Template.viewPage.helpers({
  dataset() {
    return Datasets.findOne({id: this.group});
  },
  availableLabelings() {
    return Object.keys(this.labels || {});
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
  idoc.write(this.data.source_with_blocks);

  this.autorun(() => {
    let data = Router.current().data();
    let labeling = Iron.controller().state.get('cur_labeling');
    if (!labeling) return;
    PageBlocks.markBlocks(idoc, data.labels[labeling]);
  });

  const _zoomStyleNode = PageBlocks.addStyleString(idoc,PageBlocks.zoomStyle(1));
  this.autorun(() => {
    console.log("set zoom level");
    _zoomStyleNode.innerHTML = PageBlocks.zoomStyle(Session.get('_zoomLevel'));
  });

  PageBlocks.deactivateLinks(idoc);

  PageBlocks.addStyleString(idoc, PageBlocks.pageCss);

};