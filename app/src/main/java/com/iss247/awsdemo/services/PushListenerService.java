package com.iss247.awsdemo.services;

//import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by chaitanya-iss247 on 11/4/18.
 */

public class PushListenerService {
}
//        extends GcmListenerService {
//
//    public static final String LOGTAG = PushListenerService.class.getSimpleName();
//
//    // Intent action used in local broadcast
//    public static final String ACTION_PUSH_NOTIFICATION = "push-notification";
//    // Intent keys
//    public static final String INTENT_SNS_NOTIFICATION_FROM = "from";
//    public static final String INTENT_SNS_NOTIFICATION_DATA = "data";
//
//    /**
//     * Helper method to extract push message from bundle.
//     *
//     * @param data bundle
//     * @return message string from push notification
//     */
//    public static String getMessage(Bundle data) {
//        // If a push notification is sent as plain
//        // text, then the message appears in "default".
//        // Otherwise it's in the "message" for JSON format.
//        return data.containsKey("default") ? data.getString("default") : data.getString(
//                "pinpoint.notification.body", "");
//    }
//
//    private void broadcast(final String from, final Bundle data) {
//        Log.e("PushListenerService", " message: " + (data.containsKey("default") ? data.getString("default") : data.getString(
//                "pinpoint.notification.body", "")));
//        Intent intent = new Intent(ACTION_PUSH_NOTIFICATION);
//        intent.putExtra(INTENT_SNS_NOTIFICATION_FROM, from);
//        intent.putExtra(INTENT_SNS_NOTIFICATION_DATA, data);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//    }
//
//    @Override
//    public void onMessageReceived(final String from, final Bundle data) {
//        Log.d(LOGTAG, "From:" + from);
//        Log.d(LOGTAG, "Data:" + data.toString());
//
//        final NotificationClient notificationClient =
//                ActPush.pinpointManager.getNotificationClient();
//
//        NotificationClient.CampaignPushResult pushResult =
//                notificationClient.handleGCMCampaignPush(from, data, this.getClass());
//
//        if (!NotificationClient.CampaignPushResult.NOT_HANDLED.equals(pushResult)) {
//            Log.d(LOGTAG, " CampaignPushResult not handled");
//            // The push message was due to a Pinpoint campaign.
//            // If the app was in the background, a local notification was added
//            // in the notification center. If the app was in the foreground, an
//            // event was recorded indicating the app was in the foreground,
//            // for the demo, we will broadcast the notification to let the main
//            // activity display it in a dialog.
//
//            //TODO check it afterwards, for now send message data directly
////            if (NotificationClient.CampaignPushResult.APP_IN_FOREGROUND.equals(pushResult)) {
////                // Create a message that will display the raw
////                //data of the campaign push in a dialog.
////                data.putString(" message",
////                        String.format("Received Campaign Push:\n%s", data.toString()));
////                broadcast(from, data);
////            }
//
//            data.putString(" message",
//                    String.format("Received Campaign Push:\n%s", data.toString()));
//
//            Log.d(LOGTAG, " Send data to activity");
//            broadcast(from, data);
//            return;
//        }
//    }
//}
