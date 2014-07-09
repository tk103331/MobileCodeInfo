package com.dreamlacus.mobilecodeinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.dreamlacus.mobilecodeinfo.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;  
import android.provider.ContactsContract.CommonDataKinds.Phone; 
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private DBOpenHelper DBHelper = null;
	private CodeInfo codeInfo = null;
	TextView mMobileCode;
	TextView mMobileCodeInfo;
	GridView gridview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMobileCode = (TextView) findViewById(R.id.mobileCode);
		mMobileCodeInfo = (TextView) findViewById(R.id.mobileCodeInfo);
		gridview = (GridView) findViewById(R.id.inputCode);
		fillGridView();
		copyDBFile();
		
		DBHelper = new DBOpenHelper(this);
		codeInfo = new CodeInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage(R.string.app_about);
		builder.setTitle(R.string.app_name);
		builder.create().show();
		return super.onMenuItemSelected(featureId, item);
	}
	
	public void readContacts(View v){
		int PICK_CONTACT = 3; 
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setData(Contacts.CONTENT_URI);
		startActivityForResult(intent,PICK_CONTACT);
	}
	
	public void onActivityResult(int reqCode, int resultCode, Intent data){
		super.onActivityResult(reqCode, resultCode, data);
		switch(reqCode){
		case(3):
			if (resultCode == Activity.RESULT_OK) {
//				Uri contactData = data.getData();
//				ContentResolver resolver = this.getContentResolver();
//				Cursor phoneCursor = resolver.query(contactData,new String[]{Phone.NUMBER}, null, null, null);
//				if (phoneCursor != null) {
//					while (phoneCursor.moveToNext()) {
//						String phoneNumber = phoneCursor.getString(0);
//						mMobileCode.setText(phoneNumber);
//					}
//				}
				String usernumber=""; 
	            ContentResolver reContentResolverol = getContentResolver();  
	            Uri contactData = data.getData();  
	            @SuppressWarnings("deprecation")  
	            Cursor cursor = managedQuery(contactData, null, null, null, null);  
	            cursor.moveToFirst();  
	            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));  
	            Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,   
	                     null,   
	                     ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,   
	                     null,   
	                     null);  
	             while (phone.moveToNext()) {  
	                 usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));  
	             }
	             usernumber=usernumber.replace("-", "").replace(" ","");
	             mMobileCode.setText(usernumber);
	             queryInfo();
			}
		}
	}
	private void fillGridView() {
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 1; i <= 12; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (i == 10) {
				map.put("ItemText", "Clr");
			} else if (i == 11) {
				map.put("ItemText", "0");
			} else if (i == 12) {
				map.put("ItemText", "Del");
			} else {
				map.put("ItemText", "" + i);
			}
			listItem.add(map);

		}

		SimpleAdapter saItems = new SimpleAdapter(this, listItem,
				R.layout.item, new String[] { "ItemText" },
				new int[] { R.id.inputItem });
		gridview.setAdapter(saItems);
		gridview.setOnItemClickListener(new ItemClickListener());
	}

	public void getMobileCodeInfo(View v) {
		new Thread() {
			public void run() {

				String info = getInfo(mMobileCode.getText().toString());
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putCharSequence("info", info);
				msg.setData(b);
				handler.sendMessage(msg);

			}
		}.start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String info = msg.getData().getString("info");
			if (info == null) {
				showToast("获取失败！");
				return;
			} else if (info.indexOf("没有") >= 0) {
				info = "没有此号码记录!";
			} else if (info.indexOf("http") >= 0) {
				info = "超出使用次数限制!";
			} else {
				info = info.substring(info.indexOf("：") + 1);

				try {
					if (Character.isDigit(info.split(" ")[1].charAt(0))) {
						codeInfo.setCity(info.split(" ")[0]);
						codeInfo.setCode(info.split(" ")[1]);
					} else {
						codeInfo.setCity(info.split(" ")[0]+" "
								+ info.split(" ")[1]);
						codeInfo.setCode(" ");
					}
					codeInfo.setCardtype(info.split(" ")[2]);
					DBHelper.insertCodeInfo(codeInfo);
					info = info.replace(" ", "\n");

				} catch (Exception e) {
					info = "错误";
				}

			}
			mMobileCodeInfo.setText(info);
		}

	};

	public String getInfo(String code) {

//		HttpConn conn = new HttpConn(this);
//		String s = conn.GetCodeInfo(code);
//		if (s != null && !"".equals(s)) {
//			return s;
//		}

		HttpConnSoap Soap = new HttpConnSoap(this);
		ArrayList<String> parameList = new ArrayList<String>();
		ArrayList<String> valuesList = new ArrayList<String>();
		ArrayList<String> resultList = new ArrayList<String>();
		parameList.clear();
		valuesList.clear();
		resultList.clear();
		parameList.add("mobileCode");
		valuesList.add(code);
		resultList = Soap.GetWebService("getMobileCodeInfo", parameList,
				valuesList);
		if (resultList == null) {
			return null;
		}
		return resultList.get(0);
	}

	public class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// HashMap<String, Object> item=(HashMap<String, Object>)
			// parent.getItemAtPosition(position);
			// String input=item.get("ItemText").toString();
			String input = "";
			int in = position + 1;
			switch (in) {
			case 10:
				input = "Clr";
				break;
			case 11:
				input = "0";
				break;
			case 12:
				input = "Del";
				break;
			default:
				input = String.valueOf(in);
			}
			String current = mMobileCode.getText().toString();
			if (input.equals("Del")) {
				if (current.length() > 0) {
					current = current.substring(0, current.length() - 1);
				}
			} else if (input.equals("Clr")) {
				current = "";
			} else {
				current = current + input;
			}
			mMobileCode.setText(current);
			queryInfo();
		}

	}
	
	public void queryInfo(){
		String current=mMobileCode.getText().toString();
		if (current.length() >= 7) {
			String num = current.substring(0, 7);
			if (!num.equals(codeInfo.getNum())) {
				codeInfo = DBHelper.queryCodeInfo(num);
				if (codeInfo != null) {
					String info="";
					if(codeInfo.getCity().indexOf(" ")>0){
						info=codeInfo.getCity().split(" ")[0] + "\n" + codeInfo.getCity().split(" ")[1] + "\n"+ codeInfo.getCardtype();
					}else{
						info=codeInfo.getCity() + "\n区号:"+ codeInfo.getCode() + "\n"+ codeInfo.getCardtype();
					}
					mMobileCodeInfo.setText(info);
					
				} else {
					codeInfo = new CodeInfo();
					codeInfo.setNum(num);
					getMobileCodeInfo(mMobileCode);
				}

			}
		}
	}
	private void copyDBFile(){
		//String DB_PATH ="/data/data/com.dreamlacus.mobilecodeinfo/databases/";
	    String DB_PATH = Environment.getExternalStorageDirectory()+"/dreamlacus/";
	    String DB_NAME = "mobilecodeinfo.db";
		if ((new File(DB_PATH + DB_NAME)).exists() == false) {
			File f = new File(DB_PATH);
            if (!f.exists()) {
                f.mkdir();
            }
            try {
            	InputStream is = this.getAssets().open(DB_NAME);
            	OutputStream os = new FileOutputStream(DB_PATH + DB_NAME);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                os.close();
                is.close();
            }catch(Exception e){
            	e.printStackTrace();
            }
		}
	}
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}
