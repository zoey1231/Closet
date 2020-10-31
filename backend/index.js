const http = require('http');
const app = require('./app');
const config = require('./utils/config');

const LOG = require('./utils/logger');

const server = http.createServer(app);

server.listen(config.PORT, () => {
  LOG.info(`🌐Server running on port ${config.PORT}`);
});

process.on('SIGTERM', () => {
  LOG.info('SIGTERM signal received: closing HTTP server');
  server.close(() => {
    LOG.info('HTTP server closed');
  });
});

module.exports = server;
