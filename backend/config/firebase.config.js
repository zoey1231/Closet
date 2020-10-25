var admin = require('firebase-admin');

var serviceAccount = require('./closet-firebase-adminsdk.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://closet-293003.firebaseio.com',
});

module.exports.admin = admin;
