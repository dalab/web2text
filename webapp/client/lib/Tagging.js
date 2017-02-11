Tagging = {

  goToNextPage() {
    Meteor.call('nextPageToTag', (err,res) => {
      if (res == null) {
        Router.go('loading');
      }
      Router.go('tag.page',
                {dataset_id: res.dataset, id: res.doc_id} );
    });
  },

  PAGES_PER_BATCH: 10,

  labelName() {
    return `user-${Meteor.userId()}`;
  }

}