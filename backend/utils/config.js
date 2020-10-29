require('dotenv').config();
const { version } = require('../package.json');

console.log(`
 ::::::::  :::         ::::::::   ::::::::  :::::::::: ::::::::::: 
:+:    :+: :+:        :+:    :+: :+:    :+: :+:            :+:     
+:+        +:+        +:+    +:+ +:+        +:+            +:+     
+#+        +#+        +#+    +:+ +#++:++#++ +#++:++#       +#+     
+#+        +#+        +#+    +#+        +#+ +#+            +#+     
#+#    #+# #+#        #+#    #+# #+#    #+# #+#            #+#     
 ########  ##########  ########   ########  ##########     ###     `);

console.log(
  `🚀server startup time: ${new Date().toLocaleString(undefined, {
    timeZone: 'America/Vancouver',
  })}`
);
console.log(`🌲environment:${process.env.NODE_ENV} version:${version}\n`);

let PORT = process.env.PORT;
let MONGODB_URI = process.env.MONGODB_URI;

if (process.env.NODE_ENV === 'test') {
  PORT = process.env.TEST_PORT;
  MONGODB_URI = process.env.TEST_MONGODB_URI;
}

if (process.env.NODE_ENV === 'development') {
  PORT = process.env.DEV_PORT;
  MONGODB_URI = process.env.DEV_MONGODB_URI;
}

if (process.env.NODE_ENV === 'docker') {
  MONGODB_URI = process.env.DOCKER_MONGODB_URI;
}

console.log('🔢PORT:', PORT);
console.log('🔢MONGODB_URI:', MONGODB_URI);

module.exports = {
  PORT,
  MONGODB_URI,
  VERSION: version,
};
