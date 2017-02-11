Template.login.events({
  'click .start-button'(evt) {
    evt.preventDefault();
    Meteor.loginWithAmazon();
  },
  'click .start-button-github'(evt) {
    evt.preventDefault();
    Meteor.loginWithGithub();
  },
  'click .start-button-facebook'(evt) {
    evt.preventDefault();
    Meteor.loginWithFacebook();
  }
});