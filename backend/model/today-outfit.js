const mongoose = require('mongoose');
const uniqueValidator = require('mongoose-unique-validator');

const Schema = mongoose.Schema;

const todayOutfitSchema = new Schema(
  {
    // set by backend
    _id: Number, // we wil be setting _id manually --- hashing so that we do not create duplicate outfits
    returnedTime: { type: String },
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
  },
  { versionKey: false }
);

todayOutfitSchema.plugin(uniqueValidator);

todayOutfitSchema.set('toJSON', {
  transform: (document, returnedObject) => {
    returnedObject.id = returnedObject._id.toString();
    delete returnedObject._id;
    delete returnedObject.__v;
  },
});

module.exports = mongoose.model('TodayOutfit', todayOutfitSchema);
