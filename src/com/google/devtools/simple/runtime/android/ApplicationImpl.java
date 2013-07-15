/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.simple.runtime.android;

import com.google.devtools.simple.runtime.Application;
import com.google.devtools.simple.runtime.ApplicationFunctions;
import com.google.devtools.simple.runtime.Files;
import com.google.devtools.simple.runtime.Log;
import com.google.devtools.simple.runtime.components.Component;
import com.google.devtools.simple.runtime.components.Form;
import com.google.devtools.simple.runtime.components.impl.android.FormImpl;
import com.google.devtools.simple.runtime.variants.StringVariant;
import com.google.devtools.simple.runtime.variants.Variant;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
//ADDED
import android.widget.Toast; //ADDED
import android.widget.EditText; //ADDED
import android.app.AlertDialog;  //ADDED
import android.app.Dialog; //ADDED
import android.view.LayoutInflater; //ADDED
import android.content.DialogInterface; //ADDED
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.ContentValues;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;



import java.lang.String;
import java.io.IOException;  
import java.io.FileDescriptor; 
import java.io.InputStream;  
import java.net.Socket;  
import java.net.UnknownHostException;  
import android.os.Looper;
import android.os.Process;
import android.media.MediaPlayer;   
import android.media.MediaPlayer.OnCompletionListener;
import android.text.format.Time;   

import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.content.Context;  
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

//ADDED

import java.util.ArrayList;
import java.util.List;

import android.content.Context;   
import android.provider.Settings;  
/**
 * Component underlying activities and UI apps.
 *
 * <p>This is the root container of any Android activity and also the
 * superclass for for Simple/Android UI applications.
 *
 * @author Herbert Czymontek
 */
public final class ApplicationImpl extends Activity 
	implements ApplicationFunctions {

  private boolean haveMenu=false;

  private MediaPlayer vb4amp;

  public boolean Stopable = true;

  public boolean getHMenu() {
	return haveMenu;
  }

  public void setHMenu(boolean menuVal) {
	this.haveMenu=menuVal;
  }

  /**
   * Listener for distributing the Activity onResume() method to interested
   * components.
   */
  public interface OnResumeListener {
    public void onResume();
  }

  /**
   * Listener for distributing the Activity onStop() method to interested
   * components.
   */
  public interface OnStopListener {
    public void onStop();
  }

  // Activity context
  private static ApplicationImpl INSTANCE;

  // Activity resume and stop listeners
  private final List<OnResumeListener> onResumeListeners;
  private final List<OnStopListener> onStopListeners;

  // List with menu item captions
  private final List<String>  menuItems;

  // Touch gesture detector
  private GestureDetector gestureDetector;

  // Root view of application
  private ViewGroup rootView;

  // Content view of the application (lone child of root view)
  private View contentView;
  
  // Currently active form
  private FormImpl activeForm;

  /**
   * Returns the current activity context.
   *
   * @return  activity context
   */
  public static ApplicationImpl getContext() {
    return INSTANCE;
  }

  /**
   * Creates a new application.
   */
  public ApplicationImpl() {
    INSTANCE = this;

    menuItems = new ArrayList<String>();
    onResumeListeners = new ArrayList<OnResumeListener>();
    onStopListeners = new ArrayList<OnStopListener>();
  }

  @Override
  public void onCreate(Bundle icicle) {
    // Called when the activity is first created
    super.onCreate(icicle);

    gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int direction;
        int deltaX = (int) (e1.getRawX() - e2.getRawX());
        int deltaY = (int) (e1.getRawY() - e2.getRawY());

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
          // Horizontal move
          direction = deltaX > 0 ? Component.TOUCH_FLINGLEFT : Component.TOUCH_FLINGRIGHT;
        } else {
          // Vertical move
          direction = deltaY > 0 ? Component.TOUCH_FLINGUP : Component.TOUCH_FLINGDOWN;
        }

        if (activeForm != null) {
          activeForm.TouchGesture(direction);
        }
        return true;
      }

      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int direction;
        if (Math.abs(distanceX) > Math.abs(distanceY)) {
          // Horizontal move
          direction = distanceX > 0 ? Component.TOUCH_MOVELEFT : Component.TOUCH_MOVERIGHT;
        } else {
          // Vertical move
          direction = distanceY > 0 ? Component.TOUCH_MOVEUP : Component.TOUCH_MOVEDOWN;
        }

        if (activeForm != null) {
          activeForm.TouchGesture(direction);
        }
        return true;
      }

      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        if (activeForm != null) {
          activeForm.TouchGesture(Component.TOUCH_TAP);
        }
        return true;
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        if (activeForm != null) {
          activeForm.TouchGesture(Component.TOUCH_DOUBLETAP);
        }
        return true;
      }
    });



    // Initialize runtime components
    Application.initialize(this);
    Log.initialize(new LogImpl(this));
    Files.initialize(getFilesDir());

    // We need to utilize a root view so that we can remove the actual form layout container
    // and re-add inside of a scrollable view. Otherwise there is no way removing the content view.
    rootView = new android.widget.FrameLayout(this);
    setContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
        ViewGroup.LayoutParams.FILL_PARENT));

    // Get main form information and switch to it
    try {
      Bundle metaData = getPackageManager().getActivityInfo(getComponentName(),
          PackageManager.GET_META_DATA).metaData;

      String mainFormName =
          metaData.getString("com.google.devtools.simple.runtime.android.MainForm");
      Log.Info(Log.MODULE_NAME_RTL, "main form class: " + mainFormName);
      switchForm((FormImpl) getClassLoader().loadClass(mainFormName).newInstance());

    } catch (ClassNotFoundException e) {
      Log.Error(Log.MODULE_NAME_RTL, "main form class not found");
      finish();
    } catch (NameNotFoundException e) {
      Log.Error(Log.MODULE_NAME_RTL, "manifest file without main form data");
      finish();
    } catch (SecurityException e) {
      // Should not happen
      finish();
    } catch (InstantiationException e) {
      Log.Error(Log.MODULE_NAME_RTL, "failed to instantiate main form");
      finish();
    } catch (IllegalAccessException e) {
      // Should not happen
      finish();
    }
  }

  @Override
  public boolean onKeyDown(int keycode, KeyEvent event) {
    if (activeForm != null) {
      activeForm.Keyboard(keycode);
    }
    return false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
	  menu.clear();
	  setHMenu(true);
    for (String caption : menuItems) {
      menu.add(caption);
    }
    return !menuItems.isEmpty();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (activeForm != null) {
      activeForm.MenuSelected(item.getTitle().toString());
    }
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gestureDetector.onTouchEvent(event);
  }

  @Override
  protected void onResume() {
  if (Stopable==true)
	{
    super.onResume();
    for (OnResumeListener onResumeListener : onResumeListeners) {
      onResumeListener.onResume();
    }
  }
  }

  @Override
  protected void onStop() {
    
	if (Stopable==true)
	{
		super.onStop();
		for (OnStopListener onStopListener : onStopListeners) {
		  onStopListener.onStop();
		}
	}

  }

/*
	@Override
    public void GPSSetting(int seting){
	long settingVar;
	
	

	switch (seting)
	{
	case 0:
		startActivityForResult(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS),0);
		break;
	case 1:
		startActivityForResult(new Intent(Settings.ACTION_APN_SETTINGS),0);
		break;
	case 3:
		startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),0);
		break;
	case 4:
		startActivityForResult(new Intent(Settings.ACTION_APPLICATION_SETTINGS),0);
		break;
	case 5:
		startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS),0);
		break;
	case 6:
		startActivityForResult(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS),0);
		break;
	case 7:
		startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS),0);
		break;
	case 8:
		startActivityForResult(new Intent(Settings.ACTION_DISPLAY_SETTINGS),0);
		break;
	case 9:
		startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS),0);
		break;
	case 10:
		startActivityForResult(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS),0);
		break;
	case 11:
		startActivityForResult(new Intent(Settings.ACTION_LOCALE_SETTINGS),0);
		break;
	case 12:
		startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),0);
		break;
	case 14:
		startActivityForResult(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),0);
		break;
	case 15:
		startActivityForResult(new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS),0);
		break;
	case 16:
		startActivityForResult(new Intent(Settings.ACTION_SECURITY_SETTINGS),0);
		break;
	case 17:
		startActivityForResult(new Intent(Settings.ACTION_SETTINGS),0);
		break;
	case 18:
		startActivityForResult(new Intent(Settings.ACTION_SOUND_SETTINGS),0);
		break;
	case 19:
		startActivityForResult(new Intent(Settings.ACTION_WIFI_IP_SETTINGS),0);
		break;
	case 20:
		startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),0);
		break;
	case 21:
		startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS),0);
		break;
		}





	}
*/
/*
//VB4A内存卡数据库程序
//DBPath应该不包括内存卡路径

  @Override
  public void SDSQLiteEXEC(String DBPath, String DBName, String SQLSen) {
	SQLiteDatabase db;
	String sddbpath=android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+DBPath;
	File dbp=new File(sddbpath);
    File dbf=new File(DBPath+"/"+DBName);

    if(!dbp.exists()){
		dbp.mkdir();
    }
    
	boolean isFileCreateSuccess=false; 
                    
    if(!dbf.exists()){
		try{                 
			isFileCreateSuccess=dbf.createNewFile();
        } catch(IOException ioex) {
                     
        }               
    }
	db = openOrCreateDatabase(new File(DBPath+"/"+DBName, Context.MODE_PRIVATE, null);
	db.execSQL(SQLSen); 
	db.close();  
  }

  @Override
  public Variant SDSQLitePrepare(String DBPath, String DBName, String SQLSen, String SeperatorItem, String SeperatorLine) {
	SQLiteDatabase qdb = openOrCreateDatabase(DBName, Context.MODE_PRIVATE, null);  
	Cursor cursor = qdb.rawQuery(SQLSen, null);
	String tmpvalue;
	int columnCount;
	tmpvalue="";
    while (cursor.moveToNext()) {   
		columnCount = cursor.getColumnCount();   
		for (int i = 0; i < columnCount; i++) {   
			tmpvalue =tmpvalue+cursor.getString(i)+SeperatorItem;
        } 
	tmpvalue = tmpvalue+SeperatorLine;
	}	
	cursor.close();
	qdb.close();
	return StringVariant.getStringVariant(tmpvalue);
  }

//VB4A内存卡数据库程序
*/
  /**
   * Sets the given view as the content of the root view of the application
   *
   * @param view  new root view content
   */
  public void setContent(View view) {
    if (contentView != null) {
      rootView.removeView(contentView);
    }

    contentView = view;
    rootView.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
        ViewGroup.LayoutParams.FILL_PARENT));
  }

  /**
   * Checks whether the given form is the active form.
   *
   * @param form  form to check whether it is active
   * @return  {@code true} if the given form is active, {@code false} otherwise
   */
  public boolean isActiveForm(FormImpl form) {
    return form == activeForm;
  }

  /**
   * Adds the given listener to the onResume listeners.
   *
   * @param listener  listener to add
   */
  public void addOnResumeListener(OnResumeListener listener) {
    onResumeListeners.add(listener);
  }

  /**
   * Removes the given listener from the onResume listeners.
   *
   * @param listener  listener to remove
   */
  public void removeOnResumeListener(OnResumeListener listener) {
    onResumeListeners.remove(listener);
  }

  /**
   * Adds the given listener to the onStop listeners.
   *
   * @param listener  listener to add
   */
  public void addOnStopListener(OnStopListener listener) {
    onStopListeners.add(listener);
  }

  /**
   * Removes the given listener from the onStop listeners.
   *
   * @param listener  listener to remove
   */
  public void removeOnStopListener(OnStopListener listener) {
    onStopListeners.remove(listener);
  }

  // ApplicationFunctions implementation

  @Override
  public void addMenuItem(String caption) {
    if (!menuItems.contains(caption))
    {
		menuItems.add(caption);
    }

  }

  @Override
  public void switchForm(Form form) {
    FormImpl formImpl = (FormImpl) form;
    setContent(formImpl.getView());
    // Refresh title
    form.Title(form.Title());
    activeForm = formImpl;
  }


  @Override
  public boolean MenuAdded() {
	return getHMenu();
  }

  @Override
  public void SetStopable(boolean ifstop) {
	Stopable=ifstop;
  }

  @Override
  public Variant getPreference(String name) {
    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    return StringVariant.getStringVariant(preferences.getString(name, ""));
  }

  @Override
  public void ToastMessage(String msg) {
	Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_SHORT).show();
  }


  @Override
  public void Msgbox(String title,String msg,String btn) {
    new AlertDialog.Builder(this)
    .setTitle(title)
    .setMessage(msg)
	.setPositiveButton(btn,null)
    .show();
  }

  @Override
  public void VB4AMsgboxShow(String title,String msg,String btnOK, String btnNo) {
    new AlertDialog.Builder(this)
    .setTitle(title)
    .setMessage(msg)
	.setPositiveButton(btnOK,new DialogInterface.OnClickListener(){
		@Override
			public void onClick(DialogInterface dialog, int which) {
				activeForm.VB4AMsgboxClicked(0);
			}
	})
	.setNegativeButton(btnNo,new DialogInterface.OnClickListener() {
		@Override
			public void onClick(DialogInterface dialog, int which) {
				activeForm.VB4AMsgboxClicked(1);
			}
	})
    .show();
  }



  public String InpVal;

  @Override
  public Variant Inputbox(String title,String yesstr,String nostr) {

	  InpVal="";
	final EditText inputServer = new EditText(this);
	//SharedPreferences.Editor mEditor = preferences.edit();
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle(title).setIcon(android.R.drawable.ic_dialog_info).setNegativeButton(nostr, null);
	builder.setView(inputServer).setPositiveButton(yesstr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              InpVal=inputServer.getText().toString();
			  activeForm.VB4AInputBoxClicked(InpVal);
             }
			 
        });
	
	builder.show(); 

	return StringVariant.getStringVariant(InpVal); 
	//return StringVariant.getStringVariant(""); //StringVariant.getStringVariant(mEditor);
  } // Still Can Not Get Value




 @Override
 public void PlaySound(String mFile) {
	MediaPlayer mp = new MediaPlayer();
	try
	{
		mp.setDataSource(mFile);
		mp.prepare();
		mp.start();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}
	catch (IllegalStateException e) {
		e.printStackTrace();
	}
	catch (IOException e) {
		e.printStackTrace();
	}
	mp.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mp.release();
		}
	});
 }

 @Override
 public void PlayAssetSound(String mFile) {
	MediaPlayer mp = new MediaPlayer();
	try
	{
		AssetFileDescriptor aFD = this.getAssets().openFd(mFile);
		FileDescriptor fileDescriptor = aFD.getFileDescriptor();
		mp.setDataSource(fileDescriptor, aFD.getStartOffset(), aFD.getLength());
		aFD.close();
		mp.prepare();
		mp.start();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}
	catch (IllegalStateException e) {
		e.printStackTrace();
	}
	catch (IOException e) {
		e.printStackTrace();
	}
	mp.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				mp.release();
		}
	});
 }

  @Override
  public Variant GetTime() {
    Time time = new Time();     
    time.setToNow();    
	return StringVariant.getStringVariant(String.valueOf(time.hour)+":"+String.valueOf(time.minute)+":"+String.valueOf(time.second));
  }

  @Override
  public void Quit() {
	android.os.Process.killProcess(android.os.Process.myPid());
  }

  @Override
  public void SendSQL(String DBName, String SQLSen) {
	SQLiteDatabase db = openOrCreateDatabase(DBName, Context.MODE_PRIVATE, null);  
	db.execSQL(SQLSen); 
	db.close();  
  }

  @Override
  public Variant GetSQL(String DBName, String SQLSen, String SeperatorItem, String SeperatorLine) {
	SQLiteDatabase qdb = openOrCreateDatabase(DBName, Context.MODE_PRIVATE, null);  
	Cursor cursor = qdb.rawQuery(SQLSen, null);
	String tmpvalue;
	int columnCount;
	tmpvalue="";
    while (cursor.moveToNext()) {   
		columnCount = cursor.getColumnCount();   
		for (int i = 0; i < columnCount; i++) {   
			tmpvalue =tmpvalue+cursor.getString(i)+SeperatorItem;
        } 
	tmpvalue = tmpvalue+SeperatorLine;
	}	
	cursor.close();
	qdb.close();
	return StringVariant.getStringVariant(tmpvalue);
  }

  @Override
  public Variant GetDate() {
    Time time = new Time();     
    time.setToNow();    
	return StringVariant.getStringVariant(String.valueOf(time.year)+"/"+String.valueOf(time.month+1)+"/"+String.valueOf(time.monthDay));
  }
  //String.valueOf(time.year)+"/"+String.valueOf(time.month)+"/"+String.valueOf(time.monthDay)

/*
 @Override
 public void VB4AStartActivity(String activityname) {

	Intent STactivity = new Intent(activityname); 
	startActivity(STactivity);	
 }
*/
/*
  @Override
  public Variant SocketClient(String ip, Integer port, String send) {
	try{
            Socket client = new Socket(ip,port);
         }catch (UnknownHostException e){
             e.printStackTrace();
         }catch (IOException e){
             e.printStackTrace();
    }

	try{
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream());
             out.println(msg);
             out.flush();
            
         }catch(IOException e){
             e.printStackTrace();
         }

	try{
             client.close();
         }catch(IOException e){
             e.printStackTrace();
         }
	
	return in.readLine();
  }
*/
/*
  @Override
  public void NotificationShow(String title, String title2, String contex) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); 
        Notification notification = new Notification();
		notification.tickerText = title;
		notification.when = System.currentTimeMillis();  
		notification.icon = 0;
		notification.setLatestEventInfo(this,title2,contex,null);
        notification.flags|=Notification.FLAG_AUTO_CANCEL; 
        notification.defaults |= Notification.DEFAULT_SOUND; 
        manager.notify(0, notification); 
  }
*/

  @Override
  public void ReleasePlay() {
	try
	{
		vb4amp.release();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}
  }

  @Override
  public void CreatePlay(String mFile) {
 	try
	{
		vb4amp=new MediaPlayer();
		AssetFileDescriptor aFD = this.getAssets().openFd(mFile);
		FileDescriptor fileDescriptor = aFD.getFileDescriptor();
		vb4amp.setDataSource(fileDescriptor, aFD.getStartOffset(), aFD.getLength());
		aFD.close();
		vb4amp.prepare();
		//vb4amp.start();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}
	catch (IllegalStateException e) {
		e.printStackTrace();
	}
	catch (IOException e) {
		e.printStackTrace();
	}
 }

  @Override
 public void ResumePlay() {
	try
	{
		vb4amp.start();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}

 }

  @Override
 public void StopPlay() {
	try
	{
		vb4amp.stop();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}

 }

  @Override
 public void PausePlay() {
	try
	{
		vb4amp.pause();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}

 }

  @Override
 public void Play() {

	try
	{
		vb4amp.start();
	}
	catch (IllegalArgumentException e)
	{
		e.printStackTrace();
	}
	catch (IllegalStateException e) {
		e.printStackTrace();
	}
	vb4amp.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer vb4amp) {
				vb4amp.release();
		}
	});
 }

  @Override
  public void storePreference(String name, Variant value) {
    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(name, value.getString());
    editor.commit();
  }
}
