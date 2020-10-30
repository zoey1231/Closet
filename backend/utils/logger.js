const info = (...params) => {
  if (process.env.NODE_ENV !== 'test') {
    console.log('📢INFO', `[${new Date().toUTCString()}]`, ...params);
  }
};

const error = (...params) => {
  if (process.env.NODE_ENV !== 'test') {
    console.error('❌ERROR', `[${new Date().toUTCString()}]`, ...params);
  }
};

module.exports = {
  info,
  error,
};
