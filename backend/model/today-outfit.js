const mongoose = require('mongoose');

const Schema = mongoose.Schema;

const todayOutfitSchema = new Schema(
  {
    hashId: { type: Number, required: true },
    returnedTime: { type: String, required: true },
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
  },
  { versionKey: false }
);

module.exports = mongoose.model('TodayOutfit', todayOutfitSchema);
