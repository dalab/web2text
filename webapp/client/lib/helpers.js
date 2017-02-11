Template.registerHelper('formatDate', function(date) {
  return moment(date).format('DD-MM-YYYY');
});
Template.registerHelper('formatTime', function(date) {
  return moment(date).format('LT');
});
Template.registerHelper('formatDuration', function(dateA,dateB) {
  return moment.duration(dateB-dateA).humanize();
});
Template.registerHelper('isAdmin', function() {
  if (!Meteor.userId()) return false;
  else return Meteor.user().isAdmin;
});
