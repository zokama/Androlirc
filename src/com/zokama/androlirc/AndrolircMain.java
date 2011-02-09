/**
 * AndrolircMain.java
 *
 * Android front end for Lirc
 *
 * Copyright (C) 2010 Zokama <contact@zokama.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/

package com.zokama.androlirc;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AndrolircMain extends Activity {
	
	private final static String LIRCD_CONF_FILE = "/data/data/com.zokama.androlirc/lircd.conf";
	///private final static String LIRCD_CONF_FILE = "/sdcard/lircd.conf";

	// global variables
	TextView tv;
	Lirc lirc;
	ArrayAdapter<String> deviceList;
	ArrayAdapter<String> commandList;
	AudioTrack ir;
	int minBufSize;
	boolean playLock;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        //ll.setKeepScreenOn(true);
        setContentView(ll);

        ScrollView sv = new ScrollView(this);
        tv = new TextView(this);
        //tv.setBackgroundColor();
        sv.addView(tv);
        lirc = new Lirc();
        playLock = false;

    	// Initialize adapter for device spinner
        deviceList = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
    	deviceList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Initialize device spinner
        final Spinner spinDevice = new Spinner(this);
    	spinDevice.setPrompt("Select a device");
    	spinDevice.setAdapter(deviceList);

    	// Command adapter
        commandList = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
        commandList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Parse configuration file and update device adapter
        parse(LIRCD_CONF_FILE);
    	
    	spinDevice.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
       	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
       	    	updateCommandList(spinDevice.getSelectedItem().toString());
     	    }

    	    @Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    	});
    	
    	ll.addView(spinDevice);
   	
        final Spinner spinCommand = new Spinner(this);   
     	spinCommand.setPrompt("Select a command");
    	spinCommand.setAdapter(commandList);
    	ll.addView(spinCommand);
    	
        Button btn = new Button(this);
        btn.setText("Send power IR cmd");
        btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		        if (spinDevice.getSelectedItem()==null || spinCommand.getSelectedItem() == null) {
		        	Toast.makeText(getApplicationContext(), "Please select a device and a command", Toast.LENGTH_SHORT).show();
		        	return;
		        }
		        
				String device = spinDevice.getSelectedItem().toString();
		        String cmd = spinCommand.getSelectedItem().toString();
		        sendSignal(device, cmd);
			}});

        ll.addView(btn);
        ll.addView(sv);
        
        // Prepare audio buffer
	    minBufSize = AudioTrack.getMinBufferSize(48000,
	    		AudioFormat.CHANNEL_CONFIGURATION_STEREO,
	    		AudioFormat.ENCODING_PCM_8BIT);
	    
	    ir = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, 
				AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_8BIT, 
				minBufSize, AudioTrack.MODE_STREAM);
	    
	    ir.play();
    }

    
    
    
    public String selectFile(){
		
		final EditText ed = new EditText(this);
		
    	Builder builder = new Builder(this);
    	builder.setTitle("Select a file to parse");
    	builder.setView(ed);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == Dialog.BUTTON_NEGATIVE) {
					dialog.dismiss();
					return;
				}

				parse(ed.getText().toString());
			}});
			
		builder.setNegativeButton("Cancel", null);
	   	final AlertDialog asDialog = builder.create();
	  	asDialog.show();   	
    	return null;
    }

    
    
    
    public boolean parse (String config_file) {
    	
    	//Toast.makeText(getApplicationContext(), "Parsing "+config_file, Toast.LENGTH_SHORT).show();
    	
    	java.io.File file = new java.io.File(config_file);
    	if (!file.exists()) {
    		if (config_file != LIRCD_CONF_FILE)
    			Toast.makeText(getApplicationContext(), "The Selected file doesn't exist", Toast.LENGTH_SHORT).show();
    		else
    			Toast.makeText(getApplicationContext(), "Configuartion file missing", Toast.LENGTH_SHORT).show();
    		selectFile();
    		return false;
    	}
    	
    	if (lirc.parse(config_file) == 0) {
 			Toast.makeText(getApplicationContext(), "Couldn't parse the selected file", Toast.LENGTH_SHORT).show();
			selectFile();
    		return false;
    	}
    	
    	// Save the file since it has been parsed successfully
    	if (config_file != LIRCD_CONF_FILE) {
	        try {
	            FileInputStream in = new FileInputStream(config_file);
	            FileOutputStream out = new FileOutputStream(LIRCD_CONF_FILE);
	            byte[] buf = new byte[1024];
	            int i = 0;
	            while ((i = in.read(buf)) != -1) {
	                out.write(buf, 0, i);
	            }
	            in.close();
	            out.close();
	        } catch(Exception e) {
    			tv.append("Probleme saving configuration file: "+ e.getMessage());
	        }
    	}
    	
    	updateDeviceList();
    	return true;
    }
    
    
    
    
    public void updateDeviceList(){
        String [] str = lirc.getDeviceList();
        
        if (str == null){
 			Toast.makeText(getApplicationContext(), "Invalid, empty or missing config file", Toast.LENGTH_SHORT).show();
			selectFile();
    		return;
        }
        
        deviceList.clear();
        for (int i=0; i<str.length; i++){
        	Log.e("ANDROLIRC", String.valueOf(i+1)+"/"+String.valueOf(str.length)+ ": "+str[i]);
        	deviceList.add(str[i]);
        }
        
        Log.e("ANDROLIRC", "Device list successfuly updated. Number of devices: "+String.valueOf(str.length));
        updateCommandList(str[0]);
    }

    
    
    public void updateCommandList(String device){
    	String [] str = lirc.getCommandList(device);
        
    	if (str == null){
 			Toast.makeText(getApplicationContext(), "No command found for the selected device", Toast.LENGTH_SHORT).show();
    		return;
        }

    	commandList.clear();
       	for (int i=0; i<str.length; i++)
       		commandList.add(str[i]);	

        Log.e("ANDROLIRC", "Command list successfuly updated. Number of detected commands: "+String.valueOf(str.length));
    }
    
    
    
    void sendSignal(String device, String cmd) {
    	
	    byte buffer[] = lirc.getIrBuffer(device, cmd, minBufSize+1024);
		
	    if (buffer == null) {
	    	tv.append("\nError retreiving buffer\n");
	    	return;
	    }

	    ir.setStereoVolume(1, 1);
	    int res = ir.write(buffer, 0, buffer.length);

	    Log.e("BUFFER", "written/buf_size/min_buf_size: "+res+"/"+buffer.length+"/"+minBufSize);

		// Debug
//		tv.append("Minimum buffer size: "+ 
//				String.valueOf(AudioTrack.getMinBufferSize(48000,
//	    		AudioFormat.CHANNEL_CONFIGURATION_STEREO,
//	    		AudioFormat.ENCODING_PCM_8BIT))+"\n");
//		tv.append("buffer size: "+ String.valueOf(buffer.length)+"\n");
//		tv.append("bits written: "+ String.valueOf(res)+"\n");
				

		//Save sample into a raw audio file for tests
//		try {
//			FileOutputStream myOutput = new FileOutputStream("/data/data/com.zokama.androlirc/power_toggle.raw");
//			myOutput.write(buffer, 0, buffer.length);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		
		tv.append(device + ": " +cmd + " command sent\n");
	}
    
    
    
    void deleteConfigFile() {
    	java.io.File file = new java.io.File(LIRCD_CONF_FILE);
    	if (!file.exists()) 
   			Toast.makeText(getApplicationContext(), "Configuartion file missing\n" +
   					"No file to delete", Toast.LENGTH_SHORT).show();
    	else
    		if (file.delete()){
    			Toast.makeText(getApplicationContext(), "File deleted successfully", Toast.LENGTH_SHORT).show();
    			deviceList.clear();
    			commandList.clear();
    			selectFile();
    		}
    		else
    			Toast.makeText(getApplicationContext(), "Couldn't delete the file", Toast.LENGTH_SHORT).show();
    }
    
    
    
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Parse file").setIcon(android.R.drawable.ic_menu_upload);
        menu.add(0, 1, 0, "Clear conf").setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case 0:
    		selectFile();
    		break;
    	case 1:
    		deleteConfigFile();
    		break;
    	}
    	return false;
    }
}