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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

interface keyInterface
{
	String [] getKeys();
	
	boolean saveKeys( String[] keys );
}

class Key
{
	public String hash;
	public int hit;
	
	public Key( String hash )
	{
		this.hash = hash;
		this.hit = 0;
	}
	
	public Key( String hash, int hit)
	{
		this.hash = hash;
		this.hit = hit;
	}
	
	public static boolean arrayContains( Key [] keys, String hash )
	{
		for ( Key key : keys)
		{
			if ( key.hash.equals(hash))
			{
				key.hit = 1;
				return true;
			}
		}
		return false;
	}
}

class Wallet
{
	public String name;
	public long balance;
	public Key [] keys;
	public Date lastUpdated;
	
	protected void SaveWallet( PrintWriter out ) throws IOException
	{
		if ( out != null )
		{
			out.println(name);
			out.println(balance);
			out.println(lastUpdated);
	
			for (Key key : keys )
			{
				out.println(key.hash);
				out.println(key.hit);
			}
		}
	}
	// 
	public Wallet(String name, URI url) throws IOException, ParseException
	{
		this.name = name;
		ArrayList<Key> keysArray = new ArrayList<Key>();
		HttpClient client = new DefaultHttpClient();
		HttpGet hg = new HttpGet( url );
		boolean foundakey = false;

		HttpResponse resp;
		resp = client.execute( hg );
		if ( resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
		{
			HttpEntity entity = resp.getEntity();
			
			if ( entity != null )
			{
				String keyHash;
				BufferedReader bin = new BufferedReader( new InputStreamReader( entity.getContent() ) );
				
				while ( ( keyHash = bin.readLine() ) != null )
				{
					if ( keyHash.startsWith("1") && ( keyHash.length() == 33 || keyHash.length() == 34 ) )
					{
						keysArray.add( new Key( keyHash ) );
						foundakey = true;
					}
					else
					{
						throw new ParseException("Invalid Key");
					}
				}
			}
			else
			{
				throw new ParseException("No response body");
			}
		}
		else
		{
			throw new ParseException("Did not get 200 OK");
		}
		
		if ( foundakey )
		{
			keys = new Key[ keysArray.size() ];
			keysArray.toArray( keys );
			lastUpdated = new Date();
			balance = 0;
		}
		else
		{
			throw new ParseException("Did not find any keys");
		}
	}

	protected Wallet( BufferedReader in) throws IOException, NumberFormatException
	{
		name = in.readLine();
		balance = Long.parseLong( in.readLine() );
		lastUpdated = new Date(in.readLine());
		ArrayList<Key> keysArray = new ArrayList<Key>();
		String keyHash = null;

		while ( ( keyHash = in.readLine() ) != null )
		{
			int hit = Integer.parseInt( in.readLine() );
			keysArray.add( new Key( keyHash, hit ) );
		};
		
		keys = new Key[ keysArray.size()];
		keysArray.toArray( keys );
	}
	
	public static Wallet [] getStoredWallets(Context context) throws IOException
	{
		ArrayList<Wallet> walletsArray = new ArrayList<Wallet>();

		String [] files = context.fileList();
		
		if ( files.length == 0 )
			return null;
		
		for (String filename : files)
		{
			BufferedReader in = new BufferedReader( new InputStreamReader( context.openFileInput( filename ) ) );
			try
			{
				walletsArray.add( new Wallet( in ) );
			}
			catch (NumberFormatException e)
			{
			}
			in.close();
		}
		Wallet [] wallets = new Wallet[ walletsArray.size() ];
		walletsArray.toArray( wallets );
		return wallets;
	}
	
	public static void saveWallets( Wallet [] wallets, Context context) throws IOException
	{
		for (Wallet w : wallets)
		{
			PrintWriter out = new PrintWriter( new OutputStreamWriter( context.openFileOutput( w.name, Context.MODE_PRIVATE ) ) );
			w.SaveWallet( out );
			out.close();
		}
	}
}


class WalletAdapter extends BaseAdapter
{
	private Wallet [] wallets;
	private WalletActivity context;
	
	public WalletAdapter( WalletActivity c, Wallet [] w)
	{
		this.wallets = w;
		this.context = c;
	}

	@Override
	public int getCount()
	{
		return wallets.length;
	}

	@Override
	public Object getItem(int position)
	{
		return wallets[position];
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = LayoutInflater.from( context );
		
		View v = inflater.inflate( R.layout.walletlayout, null );
		v.setLongClickable( true );
		
		TextView balanceTextView = (TextView) v.findViewById(R.id.walletBalanceText);
		DecimalFormat df = new DecimalFormat("0.00");
		balanceTextView.setText(df.format( wallets[position].balance / BalanceRetriever.SATOSHIS_PER_BITCOIN ) );
		balanceTextView.setTextSize( 20 );
		balanceTextView.setTextColor( Color.GREEN );

		TextView nameTextView = (TextView) v.findViewById(R.id.walletNameText );
		nameTextView.setText(wallets[position].name);
		nameTextView.setTextColor( Color.BLACK );
		nameTextView.setTextSize( 16 );
		
		TextView lastUpdatedTextView = (TextView) v.findViewById(R.id.lastUpdatedText );
		
		lastUpdatedTextView.setTextColor( Color.GRAY );
		lastUpdatedTextView.setTextSize( 8 );
		lastUpdatedTextView.setText( "Last Updated: " + DateFormat.format("MM/dd/yy h:mmaa", wallets[position].lastUpdated ) );

		Button button = (Button) v.findViewById(R.id.updateButton);
		button.setTag(position);
		button.setOnClickListener(context);
		return v;
	}
}

public class WalletActivity extends Activity implements OnClickListener
{
	private ProgressDialog dialog;
	private Wallet[] wallets;

	private void toastMessage( String message )
	{
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.main );
        try
        {
        	wallets = Wallet.getStoredWallets( this );
        }
        catch (IOException e)
        {
        	toastMessage(e.getMessage());
        }
        
        updateWalletList();
    }
    
    public void addWallet( Wallet wallet )
    {
    	if ( wallets == null )
    	{
    		wallets = new Wallet[1];
    		wallets[0] = wallet;
    	}
    	else
    	{
        	Wallet [] newWallets = new Wallet[ wallets.length + 1 ];
        	int i = 0;
        	for (i = 0; i < wallets.length; i++ )
        	{
        		newWallets[i] = wallets[i];
        		if ( newWallets[i].name.equals( wallet.name ) )
        		{
        			// name clash!
        			wallet.name = wallet.name.concat("2");
        		}
        	}
        	newWallets[i] = wallet;
        	wallets = newWallets;
    	}
    	updateWalletList();
    }

    public void updateWalletList()
    {
        if ( wallets != null )
        {
            ListView view = (ListView) findViewById( R.id.walletListView );

	        WalletAdapter adapter = new WalletAdapter( this, wallets );
	        view.setAdapter( adapter );
	        registerForContextMenu( view );
	        try
	        {
	        	Wallet.saveWallets( wallets, this );
	        }
	        catch (IOException e)
	        {
	        	toastMessage("Unable to save wallet data " + e.getMessage());
	        }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo )
    {
    	super.onCreateContextMenu(menu, v, menuInfo);

    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.walletmenu, menu );
    }

    protected Dialog onCreateDialog( int id )
    {
    	AlertDialog.Builder builder;
    	AlertDialog alertDialog;

    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    	View layout = inflater.inflate(R.layout.urlfetch_dialog, null );
    	TextView tv = (TextView) layout.findViewById(R.id.pasteBinHelpText);
    	tv.setMovementMethod(LinkMovementMethod.getInstance());

    	final EditText hashEditText = (EditText) layout.findViewById(R.id.hashEditText);
    	final EditText nameEditText = (EditText) layout.findViewById(R.id.nameEditText);

    	builder = new AlertDialog.Builder( this );
    	builder.setView(layout);
    	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 String hash = hashEditText.getText().toString();
                 String name = nameEditText.getText().toString();
                 
                 if ( !hash.startsWith("http") )
                 {
                	 hash = "http://pastebin.com/raw.php?i=" + hash;
                 }

                 try
                 {
                	 Wallet w = new Wallet(name, new URI( hash ) );
                	 addWallet( w );
                 }
                 catch (Exception e)
                 {
                	 toastMessage(e.getMessage());
                 }
            }
        });
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
           }
       });
    	alertDialog = builder.create();
    
    	return alertDialog;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case R.id.updateItem:
    	    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    			updateWalletBalance( wallets[info.position], false );
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch (item.getItemId())
    	{
    		case R.id.addItem:
    			showDialog( 1 );
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

    public void updateWalletBalance( Wallet w, boolean fast)
    {
    	BalanceRetriever br = new BalanceRetriever( progressHandler, w, fast );
        	
    	try
    	{
	    	dialog = new ProgressDialog( this );
	    	dialog.setMessage( "Obtaining balance..." );
	    	dialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
	    	dialog.setProgress( 0 );
	    	dialog.show();
	    	
	    	Thread t = new Thread( br );
	    	t.start();
    	}
    	catch (ParseException e)
    	{
    		toastMessage( "Could not parse keys");
    	}
    }

    Handler progressHandler = new Handler() {
    	public void handleMessage( Message msg )
    	{
    		switch ( msg.what )
    		{
    			case BalanceRetriever.MESSAGE_UPDATE:
    				dialog.incrementProgressBy( 1 );
    				break;
    			case BalanceRetriever.MESSAGE_FINISHED:
    				dialog.dismiss();
    				switch (msg.arg1)
    				{
    					case BalanceRetriever.MESSAGE_STATUS_SUCCESS:
    						updateWalletList();
    						break;
    					case BalanceRetriever.MESSAGE_STATUS_NETWORK:
    						toastMessage("Network error");
    						break;
    					case BalanceRetriever.MESSAGE_STATUS_NOKEYS:
    						toastMessage("No keys at that location");
    						break;
    					case BalanceRetriever.MESSAGE_STATUS_JSON:
    						toastMessage("JSON Parse Error");
    						break;
    				}
    				break;
    			case BalanceRetriever.MESSAGE_SETLENGTH:
    				dialog.setMax( msg.arg1 );
    				break;
    		}
    	}
    };

	@Override
	public void onClick(View v)
	{
		Button pushed = (Button) v;
		int i = (Integer) pushed.getTag();
		updateWalletBalance( wallets[ i ], true );
	}
}

class tx
{
	public String hash;
	public int rec;
	public long value;
	
	public tx( String hash, int rec, long value )
	{
		// value is number of satoshis
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

class BalanceRetriever implements Runnable
{
	public static final int MESSAGE_UPDATE = 1;
	public static final int MESSAGE_FINISHED = 2;
	public static final int MESSAGE_SETLENGTH = 3;
	
	public static final int MESSAGE_STATUS_SUCCESS = 0;
	public static final int MESSAGE_STATUS_NOKEYS = 1;
	public static final int MESSAGE_STATUS_NETWORK = 2;
	public static final int MESSAGE_STATUS_JSON= 3;
	

	public static final double SATOSHIS_PER_BITCOIN = 100000000.0;
	private static final String baseUrl = "http://blockexplorer.com/q/mytransactions/";

	// number of transactions that can be queried from blockexplorer in each GET request
	// this is limited by the maximum length of a GET request
	private static final int MAX_LENGTH = 50;
	// balance is number of microbitcoins (a millionth of a bitcoin)
	private long balance;
	private Handler updateHandler;
	private Wallet wallet;
	private boolean fast;
	
	private ArrayList<String> transactions;
	private ArrayList<tx> txs;
	private ArrayList<prevout> pendingDebits;
	
	public long getFinalBalance()
	{
		return this.balance;
	}
	
	public int getNumberOfKeys()
	{
		return wallet.keys.length;
	}

	public BalanceRetriever( Handler updateHandler, Wallet wallet, boolean fast )
	{
		this.balance = 0;
		this.updateHandler = updateHandler;
		this.wallet = wallet;
		this.fast = fast;

		transactions = new ArrayList<String>();
		txs = new ArrayList<tx>();
		pendingDebits = new ArrayList<prevout>();
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
		boolean fastKeyFound = false;
		
		for (Key k : wallet.keys )
		{
			if ( !fast )
			{
				k.hit = 0;
			}
			else
			{
				if ( k.hit > 0 )
				{
					fastKeyFound = true;
				}
			}
		}
		
		// if we asked for fast, but no fast keys found, just override this
		if ( fast && !fastKeyFound )
		{
			fast = false;
		}

		try
		{
			updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_SETLENGTH, wallet.keys.length + 1, 0 ) );
			updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_UPDATE ) );
	
			for ( Key key : wallet.keys)
			{
				if ( ( i % MAX_LENGTH ) == (MAX_LENGTH - 1) )
				{
					updateBalanceFromUrl( url.substring(0, url.length() - 1 ) );
	
					url = new StringBuffer( baseUrl );
				}
	
				if ( !fast || key.hit > 0 )
				{
					url.append( key.hash );
					url.append('.');
				}
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
		catch (IOException e)
		{
			status = MESSAGE_STATUS_NETWORK;			
		}
		catch (JSONException e)
		{
			status = MESSAGE_STATUS_JSON;
		}

		updateHandler.sendMessage( updateHandler.obtainMessage(MESSAGE_FINISHED, status, 0 ) );
		wallet.balance = balance;
		wallet.lastUpdated = new Date();
	}

	private void updateBalanceFromUrl( String url ) throws IOException, JSONException
	{
		Log.i("balance", "fetching URL "+ url);
		HttpClient client = new DefaultHttpClient();
		HttpGet hg = new HttpGet( url );

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
							if ( Key.arrayContains(wallet.keys, pubKeyHash ) )
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
						long value = (long) ( outRecord.getDouble( "value" ) * SATOSHIS_PER_BITCOIN );
						// store the out transaction, this is used later on
						txs.add( new tx( txHash, i, value ) );

						// if one of our keys is there, add the balance
						if ( Key.arrayContains(wallet.keys, pubKeyHash ) )
						{
							balance += value;
						}
					}
				}
			}
		}
	}
}