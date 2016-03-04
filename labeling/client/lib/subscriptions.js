Tracker.autorun(function () {
  Meteor.subscribe('datasets');
  Meteor.subscribe("userData");
});