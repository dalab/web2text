Meteor.users.deny({
  update() {
    return true;
  }
});

Documents = new Mongo.Collection("documents");
Documents.deny({
  insert() { return true; },
  update() { return true; },
  remove() { return true; },
});

Datasets = new Mongo.Collection("datasets");
Datasets.deny({
  insert() { return true; },
  update() { return true; },
  remove() { return true; },
});

Labels = new Mongo.Collection("labels");
Labels.deny({
  insert() { return true; },
  update() { return true; },
  remove() { return true; },
});
