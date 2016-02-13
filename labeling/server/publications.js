Meteor.publish("datasets", function () {
  if (this.userId)
    return Datasets.find();
  else return null
});

Meteor.publish("pages_in_dataset", function (id) {
  if (this.userId)
    return Pages.find({group: id},{fields:{doc_id:1,url:1,group:1}});
  else return null
});

Meteor.publish("page", function (id) {
  if (this.userId)
    return Pages.find({doc_id: id},{fields:{doc_id:1,url:1,group:1,source_code:1,labels:1,cleaned:1}});
  else return null
});
