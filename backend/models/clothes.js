const mongoose = require("mongoose");

const clothSchema = mongoose.Schema({
  clothing_id: {
    type: String,
    index: true,
    unique: true,
    required: true
  },
  image_url: String,
  category: String,
  color: String,
  season: [{
    type: String,
    enum: ["Spring", "Summer", "Fall", "Winter", "All"]
  }],
  occasion: [String],
  name: String,
  updated: { 
    type: Date, 
    default: Date.now 
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: "User",
  },
});

clothSchema.set("toJSON", {
  transform: (document, returnedObject) => {
    returnedObject.id = returnedObject._id.toString();
    delete returnedObject._id;
    delete returnedObject.__v;
  },
});

module.exports = mongoose.model("Clothes", clothSchema);
