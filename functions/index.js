const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// 🔔 Notifikasi ke Admin saat ada pesanan baru
exports.sendOrderNotificationToAdmin = functions.firestore
    .document('pesanan/{pesananId}')
    .onCreate(async (snap, context) => {
        const pesanan = snap.data();
        const payload = {
            notification: {
                title: '🎉 Pesanan Baru!',
                body: `Pesanan dari ${pesanan.NamaLengkap} telah diterima.`,
                icon: 'https://example.com/icon.png',
                click_action: 'FLUTTER_NOTIFICATION_CLICK'
            },
            topic: 'admin'
        };

        try {
            await admin.messaging().send(payload);
            console.log('Notifikasi dikirim ke admin');
        } catch (error) {
            console.error('Gagal kirim notifikasi ke admin:', error);
        }
    });

// 🔔 Notifikasi ke User saat status pesanan berubah
exports.sendStatusUpdateNotification = functions.firestore
    .document('pesanan/{pesananId}')
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();
        const status = after.status;

        if (before.status !== status) {
            const userToken = after.fcmToken;
            if (!userToken) return;

            const payload = {
                notification: {
                    title: '📦 Status Pesanan Diperbarui',
                    body: `Pesanan Anda telah ${status.toLowerCase()}.`,
                    icon: 'https://example.com/icon.png',
                    click_action: 'FLUTTER_NOTIFICATION_CLICK'
                },
                token: userToken
            };

            try {
                await admin.messaging().send(payload);
                console.log(`Notifikasi status dikirim ke user: ${status}`);
            } catch (error) {
                console.error('Gagal kirim notifikasi ke user:', error);
            }
        }
    });