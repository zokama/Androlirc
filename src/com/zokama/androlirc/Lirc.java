/**
 * Lirc.java
 *
 * Lirc support class for java
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

import java.io.File;


public class Lirc {
	public static String POWER_TOGGLE = "Function18";
	
	static {
        System.loadLibrary("androlirc");
      }
	
	native int parse(String filename);
	native byte[] getIrBuffer(String irDevice, String irCode, int minBufSize);
	native String[] getDeviceList();
	native String[] getCommandList(String irDevice);
	
	Lirc (){
	 
		File dir = new File("/data/data/com.zokama.androlirc/log");
		dir.mkdirs();
	}

}
