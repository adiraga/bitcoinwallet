/**
 * Copyright 2011 Will Harris will@phase.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.phase.wallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WalletActivity extends Activity implements OnClickListener, TextWatcher
{
	private Button button;
	private ProgressDialog dialog;
	private Balance balance;
	private String currentHash;

	private void toastMessage( String message )
	{
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	private String [] getHistory()
	{
		ArrayList<String> historyList = new ArrayList<String>();
		String [] result = null;
        SharedPreferences history = getPreferences(MODE_PRIVATE);
        int i = 0;
        String hash = null;

        while ( null != (hash = history.getString("hash" +i, null)) )
        {
        	historyList.add( hash );
        	i++;
        };
        
        if ( historyList.size() > 0 )
        {
        	result = new String[ historyList.size() ];
        	
        	historyList.toArray( result );
        }

        return result;
	}
	
	private void addHistory( String hash )
	{
		if ( hash != null )
		{
			boolean found = false;
			int number = 0;
			String [] historyItems = getHistory();
			
			if ( historyItems != null )
			{
				number = historyItems.length;
	
				for ( String historyItem : historyItems )
				{
					if ( historyItem.equals( hash ) )
					{
						found = true;
						break;
					}
				}
			}
			
			if ( !found )
			{
		        SharedPreferences history = getPreferences(MODE_PRIVATE);
		        Editor ed = history.edit();
		        // add new hash at the end
		        ed.putString("hash" + number, hash);
		        // update number
		        ed.commit();
			}
		}
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        button = (Button) findViewById( R.id.balanceButton );
        button.setOnClickListener( this );

        AutoCompleteTextView ac = (AutoCompleteTextView) findViewById( R.id.AutoCompleteTextView1 );
        String [] history = getHistory();
    	ac.setSingleLine();
    	TextView helpText = (TextView) findViewById(R.id.textView2);
    	helpText.setMovementMethod(LinkMovementMethod.getInstance());

    	if ( history != null )
        {
        	ac.setText( history[0] );
        	ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, R.layout.list_item, history );
        	ac.setAdapter( adapter );
        	ac.setThreshold( 1 );
        	button.setEnabled( true );
        }

        ac.addTextChangedListener( this );
    }
    
    @Override
    public void onClick( View v )
    {
    	EditText hashInput = (EditText) findViewById( R.id.AutoCompleteTextView1 );

    	currentHash = hashInput.getText().toString();
    	
    	if ( currentHash != null )
    	{
        	balance = new Balance( progressHandler, currentHash );
        	
	    	try
	    	{
		    	dialog = new ProgressDialog( this );
		    	dialog.setMessage( "Obtaining balance..." );
		    	dialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		    	dialog.setProgress( 0 );
		    	dialog.show();
		    	
		    	Thread t = new Thread( balance );
		    	t.start();
	    	}
	    	catch (ParseException e)
	    	{
	    		toastMessage( "Could not parse keys");
	    	}
    	}

    	button.setEnabled( true );
    }

    Handler progressHandler = new Handler() {
    	public void handleMessage( Message msg )
    	{
    		switch ( msg.what )
    		{
    			case Balance.MESSAGE_UPDATE:
    				dialog.incrementProgressBy( 1 );
    				break;
    			case Balance.MESSAGE_FINISHED:
    				dialog.dismiss();
    				switch (msg.arg1)
    				{
    					case Balance.MESSAGE_STATUS_SUCCESS:
		    				DecimalFormat df = new DecimalFormat("#.###");
		    				TextView t = (TextView) findViewById( R.id.textView1 );
		    				t.setText( "Your balance is " + df.format( balance.getFinalBalance() ) );
		    				addHistory( currentHash );
    						break;
    					case Balance.MESSAGE_STATUS_NETWORK:
    						toastMessage("Network error");
    						break;
    					case Balance.MESSAGE_STATUS_NOKEYS:
    						toastMessage("No keys at that location");
    						break;
    				}
    				break;
    			case Balance.MESSAGE_SETLENGTH:
    				dialog.setMax( msg.arg1 );
    				break;
    		}
    	}
    };

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		button.setEnabled( s.toString().length() > 0 );
	}
}

class tx
{
	public String hash;
	public int rec;
	public long value;
	
	public tx( String hash, int rec, long value )
	{
		// value is number of microbitcoins (a millionth of a bitcoin)
		this.hash = hash;
		this.rec = rec;
		this.value = value;
	}
}

class prevout
{
	public String hash;
	public int rec;
	
	public prevout( String hash, int rec )
	{
		this.hash = hash;
		this.rec = rec;
	}
}

class Balance implements Runnable
{
	public static final int MESSAGE_UPDATE = 1;
	public static final int MESSAGE_FINISHED = 2;
	public static final int MESSAGE_SETLENGTH = 3;
	
	public static final int MESSAGE_STATUS_SUCCESS = 0;
	public static final int MESSAGE_STATUS_NOKEYS = 1;
	public static final int MESSAGE_STATUS_NETWORK = 2;

	private static final String baseUrl = "http://blockexplorer.com/q/mytransactions/";

	// number of transactions that can be queried from blockexplorer in each GET request
	// this is limited by the maximum length of a GET request
	private static final int MAX_LENGTH = 50;
	// balance is number of microbitcoins (a millionth of a bitcoin)
	private long balance;
	private Handler updateHandler;
	private String pastebinHash;
	
	private ArrayList<String> keys;
	private ArrayList<String> transactions;
	private ArrayList<tx> txs;
	private ArrayList<prevout> pendingDebits;
	
	public double getFinalBalance()
	{
		return ( this.balance / 1000000.0 ) ;
	}
	
	public int getNumberOfKeys()
	{
		return keys.size();
	}

	public Balance( Handler updateHandler, String pastebinHash )
	{
		this.pastebinHash = pastebinHash;
		this.balance = 0;
		this.updateHandler = updateHandler;
		keys = new ArrayList<String>();
		transactions = new ArrayList<String>();
		txs = new ArrayList<tx>();
		pendingDebits = new ArrayList<prevout>();
	}

	public void addKey(String key)
	{
		keys.add( key );
	}
	
	public boolean addKeysFromPasteBin(String hash) throws ParseException, IOException
	{
		HttpClient client = new DefaultHttpClient();
		HttpGet hg = new HttpGet( "http://pastebin.com/raw.php?i=" + hash );
		boolean foundakey = false;

		HttpResponse resp;
		resp = client.execute( hg );
		if ( resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
		{
			HttpEntity entity = resp.getEntity();
			
			if ( entity != null )
			{
				String key;
				BufferedReader bin = new BufferedReader( new InputStreamReader( entity.getContent() ) );
				
				while ( ( key = bin.readLine() ) != null )
				{
					if ( key.startsWith("1") && ( key.length() == 33 || key.length() == 34 ) )
					{
						addKey( key );
						foundakey = true;
					}
					else
					{
						throw new ParseException();
					}
				}
			}
		}

		return foundakey;
	}
	
	private long getTxValue( String hash, int rec )
	{
		for (tx theTx : txs)
		{
			if ( theTx.hash.equals( hash ) &&
				 theTx.rec == rec )
			{
				return theTx.value;
			}
		}
		Log.e("balance", "Could not find hash " + hash + ":" + rec );
		return 0;
	}
			
	public void run()
	{
		balance = 0;
		int i = 0;
		StringBuffer url = new StringBuffer( baseUrl );
		int status = 0;
		
		try
		{
			if ( addKeysFromPasteBin( pastebinHash ) )
			{
				updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_SETLENGTH, keys.size() + 1, 0 ) );
				updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_UPDATE ) );
		
				for ( String s : keys)
				{
					if ( ( i % MAX_LENGTH ) == (MAX_LENGTH - 1) )
					{
						updateBalanceFromUrl( url.substring(0, url.length() - 1 ) );
		
						url = new StringBuffer( baseUrl );
					}
		
					url.append( s );
					url.append('.');
					i++;
					updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_UPDATE) );
				}
				
				updateBalanceFromUrl( url.substring(0, url.length() - 1 ) );

				updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_UPDATE) );
		
				// look through previous transactions and debit payments
				for ( prevout previousOut : pendingDebits )
				{
					balance -= getTxValue( previousOut.hash, previousOut.rec );
				}

				status = MESSAGE_STATUS_SUCCESS;			
			}
			else
			{
				status = MESSAGE_STATUS_NOKEYS;			
			}
		}
		catch (IOException e)
		{
			status = MESSAGE_STATUS_NETWORK;			
		}

		updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_FINISHED, status, 0 ) );			
	}

	private void updateBalanceFromUrl( String url )
	{
		Log.i("balance", "fetching URL "+ url);
		HttpClient client = new DefaultHttpClient();
		HttpGet hg = new HttpGet( url );

		try
		{
			HttpResponse response = client.execute(hg);
			
			if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
			{
				JSONObject resp = new JSONObject( EntityUtils.toString( response.getEntity() ) );
				
				// JSONObject.keys() returns Iterator<String> but for some reason
				// isn't typed that way
				@SuppressWarnings("unchecked")
				Iterator<String> itr = resp.keys();
				
				// look through every transaction
				while ( itr.hasNext() )
				{
					JSONObject txObject = resp.getJSONObject( itr.next() );
					String txHash = txObject.getString("hash");
					
					// only process transaction if we haven't seen it before
					if ( !transactions.contains( txHash ) )
					{
						Log.i("balance", "Parsing txObject " + txHash );
						transactions.add( txHash );
						// find the in transaction
						JSONArray txsIn = txObject.getJSONArray("in");
						
						for ( int i = 0; i < txsIn.length(); i++ )
						{
							JSONObject inRecord = txsIn.getJSONObject( i );
							try
							{
								String pubKeyHash = inRecord.getString("address");
		
								// if one of our keys is there, we are paying :(
								if ( keys.contains( pubKeyHash ) )
								{
									JSONObject prevRecord = inRecord.getJSONObject("prev_out");
									// if we paid for part of this transaction, record this.
									pendingDebits.add( new prevout( prevRecord.getString("hash"), prevRecord.getInt("n") ) );
								}
							}
							catch ( JSONException e )
							{
								// no address.  Probably a generation transaction
							}
						}
	
						// find the out transaction
						JSONArray txsOut = txObject.getJSONArray("out");
						
						for ( int i = 0; i < txsOut.length(); i++ )
						{
							JSONObject outRecord = txsOut.getJSONObject( i );
							String pubKeyHash = outRecord.getString("address");
							// convert to microbitcoins for accuracy
							long value = (long) ( outRecord.getDouble( "value" ) * 1000000.0 );
							// store the out transaction, this is used later on
							txs.add( new tx( txHash, i, value ) );

							// if one of our keys is there, add the balance
							if ( keys.contains( pubKeyHash ) )
							{
								balance += value;
							}
						}
					}
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
