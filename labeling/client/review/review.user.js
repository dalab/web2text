Template.reviewUser.onCreated(function () {
  this.state = new ReactiveDict
  this.state.setDefault({i:1});
});

Template.reviewUser.events({
  'change .slider'(e) {
    console.log(e);
    Template.instance().state.set({i: e.target.value})
  }
});

Template.reviewUser.helpers({
  user: ()      => Meteor.users.findOne(
                    Iron.controller().getParams().userId),
  i: ()         => Template.instance().state.get('i'),
  labelings: () => Labels.find(
                    {},
                    {limit:1,skip:Template.instance().state.get('i')-1}
                   )
});