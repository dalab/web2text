Meteor.methods({

  setLabels(dataset, doc_id, labels) {

    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    check(labels, [Match.OneOf(0,1)]);
    check(doc_id, String);
    check(dataset, String);

    const label_name = `user-${Meteor.userId()}`;
    const query = {doc_id, label_name};

    if (Documents.find(query).length > 0) {
      // update
      const update = {'$set': {labels}};
      update['$set']['metadata.last_edited'] = Date.now();
      Labels.update(query, update);
    } else {
      // insert
      const metadata = {started: Date.now(), finished: false, last_edited: Date.now()}
      const data = {doc_id, label_name, labels, dataset, metadata, user_generated: true}
      Labels.insert(data);
    }
  },

  tagBlocks(doc_id, updates) {

    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    check(doc_id, String);
    check(updates, [{block_id: Match.Integer, tag: Match.OneOf(0,1)}]);

    const label_name = `user-${Meteor.userId()}`;
    const query = {doc_id, label_name};

    [].forEach.call(updates, ({block_id, tag}) => {
      const set = {}
      set['metadata.last_edited'] = Date.now();
      set[`labels.${block_id}`] = tag;

      console.log(`[User ${Meteor.userId()}] Tagging block ${block_id} in ${doc_id} as ${tag}.`);

      const update = {'$set': set};
      Labels.update(query, update);
    });

  },

  markDone(doc_id, done) {
    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    const label_name = `user-${Meteor.userId()}`;
    const query = {doc_id, label_name};
    const update = {'$set': {}};
    update['$set']['metadata.finished'] = done;

    Labels.update(query, update);
  }
});