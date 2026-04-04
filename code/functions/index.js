const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();

// 1. PUSH NOTIFICATION DISPATCHER
// Triggers anytime a Notification document is created in Firestore
exports.sendPushNotification = onDocumentCreated("notifications/{notificationId}", async (event) => {
    const snap = event.data;
    if (!snap) return null;

    const notificationData = snap.data();
    const userId = notificationData.userId;
    const text = notificationData.text;
    const eventId = notificationData.eventId || "";

    const userDoc = await admin.firestore().collection("users").doc(userId).get();
    if (!userDoc.exists) return null;

    const user = userDoc.data();
    const token = user.fcmToken;

    // Abort if user opted out or lacks a token
    if (!token || user.notificationsOptOut === true) return null;

    // Use 'notification' payload so OS handles the banner
    const payload = {
        notification: {
            title: "Lottofy",
            body: text,
        },
        data: {
            TARGET_TAB: "MyEvents",
            eventId: eventId
        },
        android: {
            priority: "high",
            notification: {
                channelId: "zephyr_events_channel"
            }
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

// 2. AUTOMATED SCHEDULED LOTTERY
// Runs every minute, checking for Events where the deadline has passed
exports.runScheduledLotteries = onSchedule("every 1 minutes", async (event) => {
    const now = Date.now();

    // Find events that haven't been closed but their registration deadline is in the past
    const eventsSnapshot = await admin.firestore().collection("events")
        .where("registrationEndTime", "<=", now)
        .get();

    if (eventsSnapshot.empty) return;

    const promises =[];

    for (const eventDoc of eventsSnapshot.docs) {
        const eventData = eventDoc.data();
        const eventId = eventDoc.id;
        const status = eventData.status;

        // Skip if already closed, cancelled, or hasn't officially opened yet
        if (status === "CLOSED" || status === "CANCELLED" || status === "COMPLETED") {
            continue;
        }

        console.log(`Running automated lottery for Event: ${eventData.name}`);

        // Mark event as CLOSED so we don't process it again next minute
        promises.push(eventDoc.ref.update({ status: "CLOSED" }));

        // Fetch waitlisted entrants
        const waitlistSnapshot = await admin.firestore().collection("waitlist")
            .where("eventId", "==", eventId)
            .where("status", "==", "WAITLISTED")
            .get();

        let eligible =[];
        waitlistSnapshot.forEach(doc => {
            eligible.push({ id: doc.id, userId: doc.data().userId });
        });

        // If no one is on the waitlist, we just close the event and move on
        if (eligible.length === 0) continue;

        // Shuffle Array (Fisher-Yates)
        for (let i = eligible.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [eligible[i], eligible[j]] = [eligible[j], eligible[i]];
        }

        const capacity = eventData.capacity || 0;
        const winnersCount = Math.min(capacity > 0 ? capacity : eligible.length, eligible.length);

        const winners = eligible.slice(0, winnersCount);
        const losers = eligible.slice(winnersCount);

        // Process Winners
        for (const winner of winners) {
            promises.push(admin.firestore().collection("waitlist").doc(winner.id).update({ status: "SELECTED" }));
            promises.push(admin.firestore().collection("notifications").add({
                notificationId: admin.firestore().collection("notifications").doc().id,
                userId: winner.userId,
                eventId: eventId,
                type: "WON_EVENT",
                text: `Congrats! You've been chosen for "${eventData.name}". Confirm your spot today.`,
                sent: false,
                read: false,
                time: now
            }));
        }

        // Process Losers
        for (const loser of losers) {
            promises.push(admin.firestore().collection("waitlist").doc(loser.id).update({ status: "LOST" }));
            promises.push(admin.firestore().collection("notifications").add({
                notificationId: admin.firestore().collection("notifications").doc().id,
                userId: loser.userId,
                eventId: eventId,
                type: "LOST_EVENT",
                text: `The draw for "${eventData.name}" is complete. You were not selected this time.`,
                sent: false,
                read: false,
                time: now
            }));
        }

        // Notify Organizer
        if (eventData.organizerId) {
            promises.push(admin.firestore().collection("notifications").add({
                notificationId: admin.firestore().collection("notifications").doc().id,
                userId: eventData.organizerId,
                eventId: eventId,
                type: "LOTTERY_COMPLETED",
                text: `The automated lottery for "${eventData.name}" has successfully completed.`,
                sent: false,
                read: false,
                time: now
            }));
        }
    }

    await Promise.all(promises);
    console.log("Lottery processing completed.");
});