Meteor.publish("datasets", function () {
  if (this.userId)
    return Datasets.find();
  else return null
});

Meteor.publish("pages_in_dataset", function (id) {
  check(id, String);
  if (this.userId)
    return Documents.find({dataset: id},{fields:{doc_id:1,url:1,dataset:1}});
  else return null
});

Meteor.publish("page", function (id) {
  check(id, String);
  if (this.userId)
    return Documents.find({doc_id: id},{fields:{doc_id:1,url:1,dataset:1,blocked_source:1}});
  else return null
});

Meteor.publish("labels_for_page", function (doc_id) {
  check(doc_id, String);
  if (this.userId)
    return Labels.find({doc_id},{doc_id:1,_id:0,labels:1,label_name:1});
  else return null
});
