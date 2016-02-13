Meteor.subscribe('datasets');

Meteor.startup(function(){
    Session.set('page_no', 0);
});

Template.view.helpers({
  datasets: Datasets.find()
});

Template.view.events({
  'click a'() {
    Router.go('view.dataset',{id:this.id})
  }
});



Template.viewDataset.helpers({
  pages() {
    return Pages.find( {group: this.id}, {sort: {doc_id: 1}} );
  },
  hostname(url) {
    var anchor = document.createElement('a');
    anchor.href = url;
    return anchor.hostname;
  }
});

Template.viewDataset.events({
  'click .go-to-page'() {
    Router.go('view.page',{dataset_id: this.group, id: this.doc_id});
  }
});

Template.viewPage.helpers({
  dataset() {
    return Datasets.findOne({id: this.group});
  },
  labeling() {
    var controller = Iron.controller();
    return controller.state.get('labeling_id');
  },
  labelings() {
    var controller = Iron.controller();
    return Object.keys(Pages.findOne({doc_id: controller.params.id}).labels)
  },
  labelingSelected(lab) {
    var controller = Iron.controller();
    var curr = controller.state.get('labeling_id');
    console.log('vergelijk',lab,curr);
    if(lab == curr) return "selected";
    else return "";
  }
})

Template.layout.events({
  'change .labeling-selector'(evt) {
    var newValue = $(evt.target).val();
    console.log('go',newValue);
    Router.go(Router.current().route.getName(),{dataset_id: this.group, id: this.doc_id, labeling_id: newValue})
  }
})



Template.viewPage.rendered = function() {
  var iframe = document.getElementById("page");
  var idoc = iframe.contentDocument;
  idoc.write(this.data.source_code);
  var controller = Iron.controller();

  Tracker.autorun(() => {
    var labeling = controller.state.get('labeling_id');
    if (!labeling) return;
    var labels = this.data.labels[labeling];

    toArray(idoc.getElementsByClassName('boilerplate-text-block')).forEach((b,i) => {
      if (labels[i] == 1) {
        b.className = "boilerplate-text-block keep";
      } else {
        b.className = "boilerplate-text-block remove";
      }
    });
  });


  function addStyleString(str) {
      var node = document.createElement('style');
      node.innerHTML = str;
      iframe.contentDocument.body.appendChild(node);
  }

  addStyleString(".boilerplate-text-block { background-color: rgba(255,255,0,0.5); border:1px solid #bbb; } .boilerplate-text-block.keep { background-color: #bada55 !important; color: black !important } .boilerplate-text-block.remove{ background-color:red !important; color:white !important;}");
  addStyleString(".selection { background-color: rgba(186,218,85,.2) } .selection.remove { background-color: rgba(255,0,0,0.2)}");
  addStyleString("body, body * {-webkit-touch-callout: none; -webkit-user-select: none; -khtml-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none; }");

  [].forEach.call(idoc.getElementsByTagName('a'),(el) => {
    el.addEventListener('click', (e) => {
      e.preventDefault();
    });
  });


  Tracker.autorun(() => {
  })
}