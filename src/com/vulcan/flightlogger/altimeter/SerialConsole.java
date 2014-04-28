package com.vulcan.flightlogger.altimeter;

import java.util.ArrayList;

import com.vulcan.flightlogger.R;

import slickdevlabs.apps.usb2seriallib.AdapterConnectionListener;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.BaudRate;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.DataBits;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.ParityOption;
import slickdevlabs.apps.usb2seriallib.SlickUSB2Serial.StopBits;
import slickdevlabs.apps.usb2seriallib.USB2SerialAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SerialConsole extends Activity implements 
		OnClickListener, AdapterConnectionListener, USB2SerialAdapter.DataListener, OnItemSelectedListener{
	private Button mConnect;
	private Button mSend;
	private Button mClear;
	
	private TextView mCurrentSettings;
	private Spinner mBaudSpinner;
	private Spinner mDataSpinner;
	private Spinner mParitySpinner;
	private Spinner mStopSpinner;
	private Button mUpdateSettings;
	
	private Spinner mDeviceSpinner;
	private ArrayAdapter<CharSequence> mDeviceSpinnerAdapter;
	private ArrayList<USB2SerialAdapter> mDeviceAdapters;
	private USB2SerialAdapter mSelectedAdapter;
	private ArrayList<String> mDeviceOutputs;
	
	private Button mNUL;
	private Button mSTX;
	private Button mETX;
	private Button mACK;
	private Button mNAK;
	
	private EditText mSendBox;
	private EditText mReceiveBox;
	
	private CheckBox mShowHex;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.serial_console);
		
		mConnect = (Button)findViewById(R.id.deviceConnect);
		mConnect.setOnClickListener(this);
		mSend = (Button)findViewById(R.id.sendData);
		mSend.setOnClickListener(this);
		mClear = (Button)findViewById(R.id.clearData);
		mClear.setOnClickListener(this);
		
		mBaudSpinner = (Spinner)findViewById(R.id.baudSpinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBaudSpinner.setAdapter(adapter);
        String[] tempArray = SlickUSB2Serial.BAUD_RATES;
        for(int i=0; i<tempArray.length; i++)
        	adapter.add(tempArray[i]);
        mBaudSpinner.setSelection(SlickUSB2Serial.BaudRate.BAUD_9600.ordinal());
        
        mDataSpinner = (Spinner)findViewById(R.id.dataSpinner);
        adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDataSpinner.setAdapter(adapter);
        tempArray = SlickUSB2Serial.DATA_BITS;
        for(int i=0; i<tempArray.length; i++)
        	adapter.add(tempArray[i]);
        mDataSpinner.setSelection(SlickUSB2Serial.DataBits.DATA_8_BIT.ordinal());
        
        mParitySpinner = (Spinner)findViewById(R.id.paritySpinner);
        adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mParitySpinner.setAdapter(adapter);
        tempArray = SlickUSB2Serial.PARITY_OPTIONS;
        for(int i=0; i<tempArray.length; i++)
        	adapter.add(tempArray[i]);
        mParitySpinner.setSelection(SlickUSB2Serial.ParityOption.PARITY_NONE.ordinal());
        
        mStopSpinner = (Spinner)findViewById(R.id.stopSpinner);
        adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStopSpinner.setAdapter(adapter);
        tempArray = SlickUSB2Serial.STOP_BITS;
        for(int i=0; i<tempArray.length; i++)
        	adapter.add(tempArray[i]);
        mStopSpinner.setSelection(SlickUSB2Serial.StopBits.STOP_1_BIT.ordinal());
        
        mCurrentSettings = (TextView)findViewById(R.id.currentSettings);
        mUpdateSettings = (Button)findViewById(R.id.updateSettings);
        mUpdateSettings.setOnClickListener(this);
		
		mDeviceSpinner = (Spinner)findViewById(R.id.deviceSpinner);
		mDeviceSpinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		mDeviceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDeviceSpinner.setAdapter(mDeviceSpinnerAdapter);
        mDeviceSpinner.setOnItemSelectedListener(this);
        
        mDeviceAdapters = new ArrayList<USB2SerialAdapter>();
        mDeviceOutputs = new ArrayList<String>();
		
		mNUL = (Button)findViewById(R.id.nul);
		mNUL.setOnClickListener(this);
		mSTX = (Button)findViewById(R.id.stx);
		mSTX.setOnClickListener(this);
		mETX = (Button)findViewById(R.id.etx);
		mETX.setOnClickListener(this);
		mACK = (Button)findViewById(R.id.ack);
		mACK.setOnClickListener(this);
		mNAK = (Button)findViewById(R.id.nak);
		mNAK.setOnClickListener(this);
		
		mSendBox = (EditText)findViewById(R.id.inputBox);
		mReceiveBox = (EditText)findViewById(R.id.outputBox);
		
		mShowHex = (CheckBox)findViewById(R.id.textAsString);
		
		SlickUSB2Serial.initialize(this);
	}

	@Override
	public void onDestroy() {
		SlickUSB2Serial.cleanup(this);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if(v==mConnect){
			SlickUSB2Serial.autoConnect(this);
		} else if(v==mSend){
			if(mSelectedAdapter==null){
				Toast.makeText(this, "No connected adapters! Click \"Connect\" to find connected adapters.", Toast.LENGTH_SHORT).show();
				return;
			}
			
			String data = mSendBox.getText().toString();
			data = data.replace("<NUL>", "\u0000");
			data = data.replace("<STX>", "\u0002");
			data = data.replace("<ETX>", "\u0003");
			data = data.replace("<ACK>", "\u0006");
			data = data.replace("<NAK>", "\u0015");

			mSelectedAdapter.sendData(data.getBytes());
			mSendBox.setText("");
		} else if(v==mClear){
			mReceiveBox.setText("");
		} else if(v==mNUL) 
			mSendBox.setText(mSendBox.getText()+"<NUL>");
		else if(v==mSTX) 
			mSendBox.setText(mSendBox.getText()+"<STX>");
		else if(v==mETX) 
			mSendBox.setText(mSendBox.getText()+"<ETX>");
		else if(v==mACK) 
			mSendBox.setText(mSendBox.getText()+"<ACK>");
		else if(v==mNAK) 
			mSendBox.setText(mSendBox.getText()+"<NAK>");
		else if(v==mUpdateSettings){
			if(mSelectedAdapter==null)
				return;
			
			mSelectedAdapter.setCommSettings(BaudRate.values()[mBaudSpinner.getSelectedItemPosition()],
					DataBits.values()[mDataSpinner.getSelectedItemPosition()],
					ParityOption.values()[mParitySpinner.getSelectedItemPosition()],
					StopBits.values()[mStopSpinner.getSelectedItemPosition()]);
			
			updateCurrentSettingsText();

			Toast.makeText(SerialConsole.this, "Updated Settings", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void updateCurrentSettingsText(){
		mCurrentSettings.setText("Current Settings: "+mBaudSpinner.getSelectedItem().toString()
				+", "+mDataSpinner.getSelectedItem().toString()
				+", "+mParitySpinner.getSelectedItem().toString()
				+", "+mStopSpinner.getSelectedItem().toString());
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		changeSelectedAdapter(mDeviceAdapters.get(position));
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}
	
	private void changeSelectedAdapter(USB2SerialAdapter adapter){
		if(mSelectedAdapter!=null)
			mDeviceOutputs.set(mDeviceSpinnerAdapter.getPosition(mSelectedAdapter.getDeviceId()+""), mReceiveBox.getText().toString());
		
		mSelectedAdapter = adapter;
		mBaudSpinner.setSelection(adapter.getBaudRate().ordinal());
		mDataSpinner.setSelection(adapter.getDataBit().ordinal());
		mParitySpinner.setSelection(adapter.getParityOption().ordinal());
		mStopSpinner.setSelection(adapter.getStopBit().ordinal());
		updateCurrentSettingsText();
		
		mReceiveBox.setText(mDeviceOutputs.get(mDeviceSpinner.getSelectedItemPosition()));
		Toast.makeText(this, "Adapter switched to "+adapter.getDeviceId()+"!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onAdapterConnected(USB2SerialAdapter adapter) {
		adapter.setDataListener(this);
		mDeviceAdapters.add(adapter);
		mDeviceOutputs.add("");
		mDeviceSpinnerAdapter.add(""+adapter.getDeviceId());
		mDeviceSpinner.setSelection(mDeviceSpinnerAdapter.getCount()-1);
		
		Toast.makeText(this, "Adapter "+adapter.getDeviceId()+" Connected!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onAdapterConnectionError(int error, String msg) {
		if(error==AdapterConnectionListener.ERROR_UNKNOWN_IDS){
			final AlertDialog dialog = new AlertDialog.Builder(this)
			.setIcon(0)
			.setTitle("Choose Adapter Type")
			.setItems(new String[]{"Prolific", "FTDI"}, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(which==0)
						SlickUSB2Serial.connectProlific(SerialConsole.this);
					else 
						SlickUSB2Serial.connectFTDI(SerialConsole.this);
				}
			}).create();
			dialog.show();
			return;
		}
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDataReceived(int id, byte[] data) {
		final String newText = mReceiveBox.getText().toString()+" "+
				(mShowHex.isChecked()?SlickUSB2Serial.convertByte2String(data):new String(data));
		runOnUiThread(new Runnable(){
			public void run(){
				mReceiveBox.setText(newText);
				mReceiveBox.setSelection(newText.length());
			}
		});
	}
}