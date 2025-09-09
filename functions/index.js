    /**
     * Import function triggers from their respective submodules:
     *
     * const {onCall} = require("firebase-functions/v2/https");
     * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
     *
     * See a full list of supported triggers at https://firebase.google.com/docs/functions
     */

    const {onRequest} = require("firebase-functions/v2/https");
    const logger = require("firebase-functions/logger");

    // The Cloud Functions for Firebase SDK to create Cloud Functions and triggers.
    // const {logger} = require("firebase-functions");

    // The Firebase Admin SDK to access Firestore.
    // const {initializeApp} = require("firebase-admin/app");
    // const {getFirestore} = require("firebase-admin/firestore");

    // initializeApp();
    // const db = getFirestore();

    // Take the text parameter passed to this HTTP endpoint and insert it into
    // Firestore under the path /messages/:documentId/original
    exports.addmessage = onRequest(async (req, res) => {
      // Grab the text parameter.
      const original = req.query.text;
      // Push the new message into Firestore using the Firebase Admin SDK.
      // const writeResult = await getFirestore().collection("messages").add({original: original});
      // Send back a message that we've successfully written the message
      // res.json({result: `Message with ID: ${writeResult.id} added.`});
      logger.info("Hello logs!", {structuredData: true});
      res.send("Hello from Firebase!");
    });
    