package au.gov.health.covidsafe.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import au.gov.health.covidsafe.HomeActivity
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.DAILY_UPLOAD_NOTIFICATION_CODE
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_ACTIVITY
import au.gov.health.covidsafe.services.BluetoothMonitoringService.Companion.PENDING_WIZARD_REQ_CODE

class NotificationTemplates {

    companion object {

        fun getRunningNotification(context: Context, channel: String): Notification {

            val intent = Intent(context, HomeActivity::class.java)

            val activityPendingIntent = PendingIntent.getActivity(
                    context, PENDING_ACTIVITY,
                    intent, 0
            )

            val builder = NotificationCompat.Builder(context, channel)
                    .setContentTitle(context.getText(R.string.service_ok_title))
                    .setContentText(context.getText(R.string.service_ok_body))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentIntent(activityPendingIntent)
                    .setTicker(context.getText(R.string.service_ok_body))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getText(R.string.service_ok_body)))
                    .setWhen(System.currentTimeMillis())
                    .setSound(null)
                    .setVibrate(null)
                    .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

        fun lackingThingsNotification(context: Context, channel: String): Notification {


            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra("page", 3)

            val activityPendingIntent = PendingIntent.getActivity(
                    context, PENDING_WIZARD_REQ_CODE,
                    intent, 0
            )

            val builder = NotificationCompat.Builder(context, channel)
                    .setContentTitle(context.getText(R.string.service_not_ok_title))
                    .setContentText(context.getText(R.string.service_not_ok_body))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getText(R.string.service_not_ok_body)))

                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(R.drawable.ic_notification_warning)
                    .setTicker(context.getText(R.string.service_not_ok_body))
                    .addAction(
                            R.drawable.ic_notification_setting,
                            context.getText(R.string.service_not_ok_action),
                            activityPendingIntent
                    )
                    .setContentIntent(activityPendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setSound(null)
                    .setVibrate(null)
                    .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

        fun getUploadReminder(context: Context, channel: String): Notification {
            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra("uploadNotification", true)

            val activityPendingIntent = PendingIntent.getActivity(
                    context, DAILY_UPLOAD_NOTIFICATION_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, channel)
                    .setContentTitle(context.getText(R.string.upload_your_data_title))
                    .setContentText(context.getText(R.string.upload_your_data_description))
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setTicker(context.getText(R.string.upload_your_data_description))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(context.getText(R.string.upload_your_data_description)))
                    .setAutoCancel(true)

                    .addAction(
                            R.drawable.ic_notification_setting,
                            context.getText(R.string.upload_data_action),
                            activityPendingIntent
                    )
                    .setContentIntent(activityPendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setSound(null)
                    .setVibrate(null)
                    .setColor(ContextCompat.getColor(context, R.color.notification_tint))

            return builder.build()
        }

    }

}
