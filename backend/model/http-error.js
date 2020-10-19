class HttpError extends Error {
  // include both a message and an error code in the http error
  constructor(message, errorCode) {
    super(message);
    this.code = errorCode;
  }
}

module.exports = HttpError;
