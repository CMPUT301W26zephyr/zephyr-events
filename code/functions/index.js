const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendPushNotification = onDocumentCreated("notifications/{notificationId}", async (event) => {
    const snap = event.data;
    if (!snap) return null;

    const notificationData = snap.data();
    const userId = notificationData.userId;
    const text = notificationData.text;

    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (!userDoc.exists) return null;

    const user = userDoc.data();
    const token = user.fcmToken;

    if (!token || user.notificationsOptOut === true) return null;

    // Use 'notification' so the OS automatically shows the banner in the background
    // Use 'data' to pass the routing instructions to the app when clicked
    const payload = {
        notification: {
            title: "Zephyr Events",
            body: text,
        },
        data: {
            TARGET_TAB: "MyEvents"
        },
        token: token
    };

    try {
        await admin.messaging().send(payload);
        console.log("Push notification sent successfully to", userId);
        return snap.ref.update({ sent: true });
    } catch (error) {
        console.error("Error sending push notification:", error);
        return null;
    }
});