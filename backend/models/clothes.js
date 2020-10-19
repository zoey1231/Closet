const mongoose = require('mongoose');

const clothSchema = mongoose.Schema({
  // id will be mongodb generated id

  // required from request
  category: {
    type: String,
    required: true,
  },
  color: {
    type: String,
    required: true,
  },
  seasons: [
    {
      type: String,
      required: true,
      enum: ['Spring', 'Summer', 'Fall', 'Winter', 'All'],
    },
  ],
  occasions: [
    {
      type: String,
      required: true,
    },
  ],

  // optional
  name: {
    type: String,
    required: false,
  },

  // set by backend
  image_url: String,
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  updated: {
    type: Date,
    default: Date.now,
  },
});

clothSchema.set('toJSON', {
  transform: (document, returnedObject) => {
    returnedObject.id = returnedObject._id.toString();
    delete returnedObject.__v;
  },
});

module.exports = mongoose.model('Clothes', clothSchema);
