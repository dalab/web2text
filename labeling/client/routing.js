Router.configure({
  layoutTemplate: 'layout'
});

Router.route('/', {
  name: 'start',
  action() {
    Tagging.goToNextPage();
  }
});

Router.route('/view', {
  loadingTemplate: 'loading',
  template: 'view',
  name: 'view.index',
  action() {
    this.render();
  }
});
Router.route('/done-tagging', {
  loadingTemplate: 'loading',
  template: 'doneTagging',
  name: 'done.tagging',
  action() {
    this.render();
  }
});

Router.route('/view/:id', {
  template: 'viewDataset',
  name: 'view.dataset',
  data() {
    return Datasets.findOne({id: this.params.id});
  },
  subscriptions() {
    return Meteor.subscribe('pages_in_dataset',this.params.id);
  },
  action() {
    if (this.ready()) {
      this.render();
    } else {
      this.render('loading');
    }
  }
});

Router.route('/view/:dataset_id/:id/:cur_labeling?', {
  loadingTemplate: 'loading',
  template: 'viewPage',
  name: 'view.page',
  subscriptions() {
    return [Meteor.subscribe('page',this.params.id),
            Meteor.subscribe('pages_in_dataset',this.params.dataset_id),
            Meteor.subscribe('labels_for_page',this.params.id)];
  },
  data() {
    return Documents.findOne({doc_id: this.params.id});
  },
  action() {
    if (this.ready()) {
      let labs = Labels.find({doc_id:this.params.id,$or: [{"metadata.finished": true},{user_generated:false}]}).fetch().map(x => x.label_name);
      let pars = this.params;
      let cur_labeling = pars.cur_labeling
      if (!cur_labeling || labs.indexOf(cur_labeling) == -1) {
        if (labs.length > 0) {
          return Router.go( 'view.page',
                            {dataset_id: pars.dataset_id,
                             id: pars.id, cur_labeling: labs[0] },
                            {replaceState: true} );
        } else {
          // return Router.go( 'view.dataset',
          //                   {id: pars.dataset_id},
          //                   {replaceState: true} );
        }
      }
      this.state.set('cur_labeling',cur_labeling);
      this.render();
    } else {
      this.state.set('cur_labeling',null);
      this.render('loading');
    }
  }
});

Router.route('/tag/:dataset_id/:id', {
  loadingTemplate: 'loading',
  template: 'tagPage',
  name: 'tag.page',
  subscriptions() {
    return [Meteor.subscribe('page',this.params.id),
            Meteor.subscribe('pages_in_dataset',this.params.dataset_id),
            Meteor.subscribe('labels_for_page',this.params.id)];
  },
  data() {
    return Documents.findOne({doc_id: this.params.id});
  },
  action() {
    if (this.ready()) {
      let cur_labeling = 'user-'+Meteor.userId();

      this.state.set('cur_labeling',cur_labeling);
      this.render();
    } else {
      this.render('loading');
    }
  }
});

Router.onBeforeAction(function () {
  if (!Meteor.userId()) {
    this.layout()
    this.render('login');
  } else {
    this.next();
  }
});
