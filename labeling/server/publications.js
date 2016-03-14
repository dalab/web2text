Meteor.publish("datasets", function () {
  if (this.userId)
    return Datasets.find();
  else return null
});

Meteor.publish("pages_in_dataset", function (id) {
  check(id, String);
  if (this.userId)
    return Documents.find({dataset: id},{fields:{doc_id:1,url:1,dataset:1,removed:1}});
  else return null
});

Meteor.publish("page", function (id) {
  check(id, String);
  if (this.userId)
    return Documents.find({doc_id: id},{fields:{doc_id:1,url:1,dataset:1,blocked_source:1,removed:1, n_blocks: 1}});
  else return null
});

Meteor.publish("pageSource", function (id) {
  check(id, String);
  console.log("Subscribe to page source for",id);
  if (this.userId)
    return Documents.find({doc_id: id},{fields:{source:1}});
  else return null
});

Meteor.publish("labels_for_page", function (doc_id) {
  check(doc_id, String);
  if (this.userId)
    return Labels.find({doc_id});
  else return null
});

Meteor.publish("users_labeling", function (doc_id) {
  check(doc_id, String);
  if (this.userId)
    return Labels.find({doc_id,label_name:`user-${this.userId}`});
  else return null
});

Meteor.publish("userData", function () {
    return Meteor.users.find({_id: this.userId},
        {fields: {'n_tagged': 1, 'isAdmin': 1}});
});

Meteor.publish("userName", function (_id) {
  check(_id,String);
    return Meteor.users.find({_id},
        {fields: {'profile.name': 1}});
});


Meteor.publish("users", function () {
    return Meteor.users.find({},
        {fields: {'n_tagged': 1, 'profile.name':1}});
});

Meteor.publish("user_label_basic_for_dataset", function (dataset) {
  check(dataset, String);
  if (this.userId)
    return Labels.find({dataset, user_generated: true},{fields:{doc_id: 1, label_name:1, "metadata.finished": 1, user_generated: 1, "metadata.skipped": 1}});
  else return null
});