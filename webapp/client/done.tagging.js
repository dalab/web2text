Template.doneTagging.helpers({
  amazonCode() {
    const userId = Meteor.userId();
    const count = Math.floor(Meteor.user().n_tagged/10)*10;
    return `${userId}_${count}`;
  }
});

Template.doneTagging.events({
  'click .amazon-code-field'(evt) {
    evt.target.select();
  },
  'click .go-again'(evt) {
    evt.preventDefault();
    Router.go("start");
  }
})