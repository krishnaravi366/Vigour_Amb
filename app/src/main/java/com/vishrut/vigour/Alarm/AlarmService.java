package com.vishrut.vigour.Alarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.vishrut.vigour.NewMessageNotification;

public class AlarmService extends Service {

	public static String TAG = AlarmService.class.getSimpleName();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Intent alarmIntent = new Intent(getBaseContext(), AlarmScreen.class);
		alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		alarmIntent.putExtras(intent);
		NewMessageNotification.notify(this,"New Reminder",0);
		getApplication().startActivity(alarmIntent);
		
		AlarmManagerHelper.setAlarms(this);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
}