Router.configure({
  layoutTemplate: 'layout'
});

Router.route('/', {
  loadingTemplate: 'loading',
  template: 'view',
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

Router.route('/view/:dataset_id/:id/:labeling_id?', {
  loadingTemplate: 'loading',
  template: 'viewPage',
  name: 'view.page',
  subscriptions() {
    return [Meteor.subscribe('page',this.params.id),
            Meteor.subscribe('pages_in_dataset',this.params.dataset_id)];
  },
  data() {
    return Pages.findOne({doc_id: this.params.id});
  },
  action() {
    if (this.ready()) {
      if (!this.params.labeling_id) {
        var labs = Object.keys(this.data().labels);
        if (labs.length > 0) {
          Router.go('view.page',{
            dataset_id: this.params.dataset_id,
            id: this.params.id,
            labeling_id: labs[0]
          });
        } else {
          Router.go('view.dataset',{id: this.params.dataset_id});
        }
      }
      console.log("Will render, set state");
      this.state.set('labeling_id',this.params.labeling_id);
      this.render();
    } else {
      this.render('loading');
    }
  }
});

Router.onBeforeAction(function () {
  if (!Meteor.userId()) {
    this.layout()
    this.render('Login');
  } else {
    this.next();
  }
});
