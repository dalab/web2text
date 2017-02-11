Template.reviewUserSingle.onCreated(function () {
  this.autorun(() => {
    this.subscribe('page',Template.currentData().doc_id);
  });
});

Template.reviewUserSingle.helpers({
  'pageContent': function () {
    const doc_id = Template.currentData().doc_id;
    const page = Documents.findOne({doc_id});
    if (!page) return "";
    return page.blocked_source;
  },
  'ready': function () {
    return Template.instance().subscriptionsReady();
  }
});