const info = (...params) => {
  if (process.env.NODE_ENV !== 'test') {
    console.log(
      'INFO',
      `[${new Date().toLocaleString('en-CA', {
        timeZone: 'America/Vancouver',
      })}]`,
      ...params
    );
  }
};

const error = (...params) => {
  if (process.env.NODE_ENV !== 'test') {
    console.error(
      '❌ERROR',
      `[${new Date().toLocaleString('en-CA', {
        timeZone: 'America/Vancouver',
      })}]`,
      ...params
    );
  }
};

module.exports = {
  info,
  error,
};
