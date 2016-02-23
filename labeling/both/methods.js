Meteor.methods({

  setLabels(doc_id, labels) {

    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    check(labels, [Match.OneOf(0,1)]);
    check(doc_id, String);

    const labeling = `user-${Meteor.userId()}`;
    const query = {doc_id};
    const set = {};
    set[`labels.${labeling}`] = labels;
    const update = {'$set': set};

    console.log('set labels');

    Pages.update(query, update);

  },

  tagBlock(tag, doc_id, block_id) {

    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    console.log(`[User ${Meteor.userId()}] Tagging block ${block_id} in ${doc_id} as ${tag}.`);


    check(tag, Match.OneOf(0,1));
    check(doc_id, String);
    check(block_id, Match.Integer);

    const labeling = `user-${Meteor.userId()}`;
    const query = {doc_id};
    const set = {};
    set[`labels.${labeling}.${block_id}`] = tag;
    const update = {'$set': set};

    Pages.update(query, update);

  },

  tagBlocks(doc_id, updates) {

    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    check(doc_id, String);
    check(updates, [{block_id: Match.Integer, tag: Match.OneOf(0,1)}]);

    const labeling = `user-${Meteor.userId()}`;
    const query = {doc_id};
    const set = {};

    [].forEach.call(updates, ({block_id, tag}) => {
      console.log(`[User ${Meteor.userId()}] Tagging block ${block_id} in ${doc_id} as ${tag}.`);
      set[`labels.${labeling}.${block_id}`] = tag;
      const update = {'$set': set};
      Pages.update(query, update);
    });

  },

  markDone(doc_id, done) {
    if (!Meteor.userId()) {
      throw new Meteor.Error("not-authorized");
    }

    const labeling = `user-${Meteor.userId()}`;
  }
});