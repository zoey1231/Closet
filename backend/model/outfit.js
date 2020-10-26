const mongoose = require('mongoose');
const uniqueValidator = require('mongoose-unique-validator');

const Schema = mongoose.Schema;

const outfitSchema = new Schema({
  // set by backend
  _id: Number, // we wil be setting _id manually --- hashing so that we do not create duplicate outfits
  clothes: [
    {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Clothes',
    },
  ],
  created: {
    type: Date,
    default: Date.now,
  },
});

outfitSchema.plugin(uniqueValidator);

outfitSchema.set('toJSON', {
  transform: (document, returnedObject) => {
    returnedObject.id = returnedObject._id.toString();
    delete returnedObject._id;
    delete returnedObject.__v;
  },
});

module.exports = mongoose.model('Outfit', outfitSchema);
