Meteor.methods({
  nextPageToTag() {

    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    // Fetch labels
    const query = {dataset: {$regex: /clueweb\d+/}, user_generated: true, "metadata.finished": true}
    const projection = {_id: 0, "doc_id": 1}
    const labs = Labels.find(query, {fields: projection}).fetch();

    // Construct a counts map
    const counts = new Map();
    const getCount = (doc_id) => {
      if (counts.has(doc_id)) return counts.get(doc_id);
      else return 0;
    };
    labs.forEach((doc) => counts.set(doc.doc_id, getCount(doc.doc_id)+1))

    // Construct a map of items that are done by the user already
    const label_name = `user-${Meteor.userId()}`;
    const done = Labels.find(
      {label_name, $or: [ {"metadata.skipped": true}, {"metadata.finished": true} ]},
      {fields:{_id:0,doc_id:1}}
    ).fetch();
    const doneMap = new Map();
    done.forEach((x) => doneMap.set(x.doc_id,true));

    const datasets = ["clueweb100", "clueweb200", "clueweb300", "clueweb400", "clueweb500", "clueweb600", "clueweb700", "clueweb800", "clueweb900", "clueweb1000", "clueweb1100"];

    let result = null;
    datasets.forEach((dataset) => {
      if (result) return;
      // Fetch documents
      const docQuery = {dataset,removed:{$ne: true}}
      const docProjection = {_id: 0, dataset: 1, doc_id: 1}
      const docs = Documents.find(docQuery, {fields: docProjection}).fetch();
      const mapped = docs.map((doc) => {
        let count = 0
        if (doneMap.has(doc.doc_id))
          count = Tagging.TARGET_COUNT+1;
        else
          count = getCount(doc.doc_id);
        return {count,doc_id: doc.doc_id}
      });
      mapped.sort(function(a, b) {
        return +(a.count > b.count) || +(a.count === b.count) - 1;
      });
      if(mapped[0].count < Tagging.TARGET_COUNT) {
        result = {doc_id: mapped[0].doc_id, dataset: dataset};
      }
    });
    return result;
  },

  removeDocument(doc_id, remove) {
    if (!Meteor.userId() || Meteor.user().services.github.email != 't.vogels@me.com') {
      throw new Meteor.Error("not-authorized");
    }
    check(doc_id, String);
    check(remove, Boolean);

    const query = {doc_id};
    const update = {'$set': {'removed': remove}};
    Documents.update(query, update);
  }
});