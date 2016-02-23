Template.viewAlignmentModal.helpers({
  formattedAlignment() {
    let str = ""
    for (var i = 0; i < this.source.length; i++) {
      let sourceChar = this.source[i];
      let alignedChar = this.aligned[i];
      if (sourceChar == alignedChar) {
        str += "<span class='keep-char'>"+sourceChar+"</span>";
      } else {
        str += "<span class='remove-char'>"+sourceChar+"</span>";
      }
    };
    return str;
  }
});