package com.dreamlacus.mobilecodeinfo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.util.Log;

public class HttpConn {
	Context parent;
	public HttpConn(Context context){
		parent=context;
	}
	public String GetCodeInfo(String phnum) {
		String ServerUrl="http://ftpdz5311.vip.web968.com/guishudi/search.php?phnum="+phnum;
		String result="";
		try {
			URL url = new URL(ServerUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "text/xml;charset=utf-8");

			InputStream inStream = con.getInputStream();
			StringBuffer out = new StringBuffer();
			String s1 = "";
			byte[] b = new byte[4096];
			for (int n; (n = inStream.read(b)) != -1;) {
				s1 = new String(b, 0, n);
				out.append(s1);
			}
			result = out.toString();
			if("no".equals(result)||"err".equals(result)){
				return "";
			}else{
				return result;
			}

		} catch (Exception e) {
			Log.i("aaa", e.toString());
			return null;
		}
	}

}
