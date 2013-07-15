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

package com.google.devtools.simple.runtime;

import com.google.devtools.simple.runtime.annotations.SimpleFunction;
import com.google.devtools.simple.runtime.annotations.SimpleObject;
import com.google.devtools.simple.runtime.components.Form;
import com.google.devtools.simple.runtime.variants.Variant;
import com.google.devtools.simple.runtime.annotations.SimpleEvent;

/**
 * Implementation of various application related runtime functions.
 * 
 * @author Herbert Czymontek
 */
@SimpleObject
public abstract class Application {

  private static ApplicationFunctions applicationFunctions;

  private Application() {
  }

  /**
   * Initializes the application functionality of the application library.
   *
   * @param functions  implementation of application functions
   */
  public static void initialize(ApplicationFunctions functions) {
    applicationFunctions = functions;
  }

  /**
   * Creates a new menu item with the given caption.
   *
   * <p>The caption will also be used to identify the menu item in the menu
   * event handler.
   *
   * @param caption  menu item caption
   */
  @SimpleFunction
  public static void AddMenuItem(String caption) {
    applicationFunctions.addMenuItem(caption);
  }

  @SimpleFunction
  public static void SetStopable(boolean ifstop) {
	applicationFunctions.SetStopable(ifstop);
  }

  @SimpleFunction
  public static void PlayMedia2(String mFile) {
    applicationFunctions.PlaySound(mFile);
  }

  @SimpleFunction
  public static void SQLEXEC(String DBName, String SQLSen) {
	  // public void SendSQL(String DBName, String SQLSen)
	applicationFunctions.SendSQL(DBName,SQLSen);
  }

  @SimpleFunction
  public static Variant SQLPREPARE(String DBName, String SQLSen, String SeperatorItem, String SeperatorLine) {
	return applicationFunctions.GetSQL(DBName, SQLSen, SeperatorItem, SeperatorLine);
  }
/*
  @SimpleFunction
  public static void SDSQLiteEXEC(String DBPath, String DBName, String SQLSen) {
	applicationFunctions.SDSQLiteEXEC(DBPath, DBName, SQLSen);
  }

  @SimpleFunction
  public static Variant SDSQLitePrepare(String DBPath, String DBName, String SQLSen, String SeperatorItem, String SeperatorLine) {
	return applicationFunctions.SDSQLitePrepare(DBPath, DBName, SQLSen, SeperatorItem, SeperatorLine);
  }
*/
  @SimpleFunction
  public static void PlayMedia(String mFile) {
    applicationFunctions.PlayAssetSound(mFile);
  }
  /**
   * Display a different form.
   *
   * @param form  form to display
   */
  @SimpleFunction
  public static void SwitchForm(Form form) {
    applicationFunctions.switchForm(form);
  }
  
  @SimpleFunction
  public static void ToastMessage(String msg) {
	applicationFunctions.ToastMessage(msg);
  }

  @SimpleFunction
  public static void Msgbox(String title,String msg,String btn) {
	applicationFunctions.Msgbox(title,msg,btn);
  }
  

  @SimpleFunction
  public static void VB4AMsgboxShow(String title,String msg,String btnYes, String btnNo) {
	applicationFunctions.VB4AMsgboxShow(title,msg,btnYes,btnNo);
  }


/*  
//  @SimpleFunction
  public static void VB4ANotify(String title, String title2, String contex) {
	applicationFunctions.NotificationShow(title, title2, contex);
  }
*/
  @SimpleFunction
  public static Variant VB4AInputBoxShow(String title,String yesstr,String nostr) {
	return applicationFunctions.Inputbox(title,yesstr,nostr);
  }

/*
  @SimpleFunction
  public Variant SocketClient(String ip, Integer port, String send) {
	return applicationFunctions.SocketClient(ip,port,send);
  }
  */
/*
//  @SimpleFunction
  public static void VB4AStartActivity(String activityname) {
	applicationFunctions.VB4AStartActivity(activityname);
  }
  @SimpleFunction
  public static void VB4ASetup(int seting) {
	applicationFunctions.GPSSetting(seting);
  }
*/
  @SimpleFunction
  public static void Quit() {
	applicationFunctions.Quit();
  }

  /**
   * Terminates this application.
   */
  @SimpleFunction
  public static void Hide() {
    applicationFunctions.finish();
  }

  @SimpleFunction
  public static void Finish() {
    applicationFunctions.finish();
  }
  /**
   * Retrieves the value of a previously stored preference (even from previous
   * of the same program).
   *
   * @param name  name which was used to store the value under
   * @return  value associated with name
   */
  @SimpleFunction
  public static Variant GetPreference(String name) {
    return applicationFunctions.getPreference(name);
  }

  @SimpleFunction
  public static Variant GetTime() {
    return applicationFunctions.GetTime();
  }

  @SimpleFunction
  public static Variant GetDate() {
    return applicationFunctions.GetDate();
  }

  @SimpleFunction
  public static boolean MenuAdded() {
	return applicationFunctions.MenuAdded();
  }
  /**
   * Stores the given value under given name. The value can be retrieved using
   * the given name any time (even on subsequent runs of the program).
   * 
   * @param name  name to store value under
   * @param value  value to store (must be a primitive value, objects not
   *               allowed)
   */
  @SimpleFunction
  public static void StorePreference(String name, Variant value) {
    applicationFunctions.storePreference(name, value);
  }

  @SimpleFunction
  public static void CreatePlay(String mFile) {
    applicationFunctions.CreatePlay(mFile);
  }

  @SimpleFunction
  public static void ReleasePlay(){
	applicationFunctions.ReleasePlay();
  }

  @SimpleFunction
  public static void Play() {
    applicationFunctions.Play();
  }
//  @SimpleFunction
  public static void ResumePlay() {
    applicationFunctions.ResumePlay();
  }
  @SimpleFunction
  public static void StopPlay() {
    applicationFunctions.StopPlay();
  }

  @SimpleFunction
  public static void PausePlay() {
	applicationFunctions.PausePlay();
  }
}
