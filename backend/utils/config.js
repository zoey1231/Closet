require('dotenv').config();
const { version } = require('../package.json');

const LOG = require('./logger');

LOG.info(`
 ::::::::  :::         ::::::::   ::::::::  :::::::::: ::::::::::: 
:+:    :+: :+:        :+:    :+: :+:    :+: :+:            :+:     
+:+        +:+        +:+    +:+ +:+        +:+            +:+     
+#+        +#+        +#+    +:+ +#++:++#++ +#++:++#       +#+     
+#+        +#+        +#+    +#+        +#+ +#+            +#+     
#+#    #+# #+#        #+#    #+# #+#    #+# #+#            #+#     
 ########  ##########  ########   ########  ##########     ###     `);

LOG.info(
  `🚀server startup time: ${new Date().toLocaleString(new Date(), {
    timeZone: 'America/Vancouver',
  })}`
);
LOG.info(`🌲environment:${process.env.NODE_ENV} version:${version}`);

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

LOG.info('🔢PORT:', PORT);
LOG.info('🔢MONGODB_URI:', MONGODB_URI);

module.exports = {
  PORT,
  MONGODB_URI,
  VERSION: version,
};
