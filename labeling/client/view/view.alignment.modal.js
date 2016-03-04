
Template.viewAlignmentModal.helpers({
  formattedAlignment() {
    let str = ""

    const docm = Documents.findOne({doc_id: this.doc_id});
    const labeling = Labels.findOne({doc_id: this.doc_id, label_name: this.label_name});

    if (!docm || !docm.source) return;

    for (var i = 0; i < docm.source.length; i++) {
      let sourceChar = docm.source[i];
      let alignedChar = labeling.metadata.aligned[i];
      if (sourceChar == alignedChar) {
        str += "<span class='keep-char'>"+sourceChar+"</span>";
      } else {
        str += "<span class='remove-char'>"+sourceChar+"</span>";
      }
    };
    return str;
  }
});

Template.viewAlignmentModal.onCreated(function () {
    this.subscribe('pageSource',this.data.doc_id);
});
