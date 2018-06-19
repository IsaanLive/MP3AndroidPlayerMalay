package com.euroasia.in;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.euroasiamp3.eula.GUtils;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends ListActivity {
	public String searchstring;
	
	private RetrieveSearch retrievesearch;
	
	public MyAdapter adapter;
	
	public int fail = 0, ia;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search); 
        
        GUtils.getGTRACKER(this).trackPageViewEvent("SearchActivity");
		//Action Bar Setup:		
		final Context thisact = this;
		
		TextView headerview = (TextView)this.findViewById(R.id.title_bar_text);
		headerview.setText("Cari");
		
		ImageView searchbutton = (ImageView)this.findViewById(R.id.action_search);
		searchbutton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				if(!thisact.toString().contains("SearchActivity")){
					Intent searchintent = new Intent(thisact, SearchActivity.class);
					searchintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(searchintent);
				}
			}
		});
		
		ImageView homeicon = (ImageView)this.findViewById(R.id.logo_icon);
		homeicon.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				if(!thisact.toString().contains("DashboardActivity")){
					Intent searchintent = new Intent(thisact, DashboardActivity.class);
					searchintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(searchintent);
				}
			}
		});

        final EditText searchEdit = (EditText)this.findViewById(R.id.searchEdit);  
        
		//Check If Variable Is Being Passed
		Intent intent = getIntent();
        if (intent.hasExtra("search")){
        	searchstring = intent.getStringExtra("search");     
            
        	//Set Action Bar
        	searchEdit.setText(searchstring);
        	
        	startsearch();   	
        }
        
        searchEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            		if(searchEdit.getText().toString().equals("")){
            			toastmake("Tiada apa-apa yang dimasukkan");
            		}else{
            			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            			imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
            			
            			searchstring = searchEdit.getText().toString();
            			startsearch();            			
            		}
                    return true;
                }
                return false;
            }
        });
        
        final ImageButton searchbtn = (ImageButton)this.findViewById(R.id.search_ib);
        searchbtn.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
        		if(searchEdit.getText().toString().equals("")){
        			toastmake("Tiada apa-apa yang dimasukkan");
        		}else{
        			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        			imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
        			
        			searchstring = searchEdit.getText().toString();
        			startsearch();            			
        		}
			}	
        });
        
        //List View Scroll Speed
        ListView lv = getListView();
        lv.setCacheColorHint(Color.TRANSPARENT); // not sure if this is required for you. 
        lv.setFastScrollEnabled(true);
        lv.setScrollingCacheEnabled(false);
        
        //Ads Test
		DisplayMetrics dm = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
		//
    }
    
    public void startsearch(){
		if(ia > 0){
			adapter = new MyAdapter(SearchActivity.this, 0);
			setListAdapter(adapter);
		}
		
		TextView failresults = (TextView)findViewById(R.id.failresults);
		failresults.setText("");
		
		ia = 0;
		searchstring = searchstring.replace("*", " ");
		searchstring = searchstring.replaceAll("[^A-Za-z0-9& ]", "");
		searchstring = searchstring.replaceAll("  ", " ");
		searchstring = searchstring.replaceAll("	", " ");
		searchstring = searchstring.replaceAll("  ", " ");

        final EditText searchEdit = (EditText)this.findViewById(R.id.searchEdit);  
        searchEdit.setText(searchstring);
        
		toastmake("Mencari untuk: "+searchstring);
			
		adapter = new MyAdapter(SearchActivity.this, 0);
		setListAdapter(adapter);
			
		retrievesearch = new RetrieveSearch();
		retrievesearch.execute();
    }
    
	@Override
	public void onBackPressed() {
	    this.finish();
	    return;
	}
	
	public void toastmake(String title){
		Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
	}	
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        AdapterItem data = adapter.items.get(position);

        String title = data.first;
        String url = data.second;
        int lid = data.lid;
        
		Intent intent = new Intent(SearchActivity.this, ViewSongActivity.class);
		
		intent.putExtra("title", title);
		intent.putExtra("url", url);
		intent.putExtra("lid", lid);
		
		startActivity(intent);
	}
	
	private class RetrieveSearch extends AsyncTask<Void, Void, Void>{
		private ProgressDialog cancelDialog = null;
	
		@Override
		protected Void doInBackground(Void... params) {
			
			adapter = new MyAdapter(SearchActivity.this, 0);
			ia = 0;

			try{
				getSoundcat(searchstring);
			} catch(IOException e) {
				e.printStackTrace();
				fail++;
			} catch(StringIndexOutOfBoundsException e){
				e.printStackTrace();
				fail++;
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				fail++;
			}
			
			try{
				getDilandau(searchstring); //seekasong.com
			} catch(IOException e) {
				e.printStackTrace();
				fail++;
			} catch(StringIndexOutOfBoundsException e){
				e.printStackTrace();
				fail++;
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				fail++;
			}
			
			return null;
		}
	
		@Override
		protected void onCancelled() {		
			super.onCancelled();
			adapter = new MyAdapter(SearchActivity.this, 0);
			setListAdapter(adapter);

			TextView failresults = (TextView)findViewById(R.id.failresults);
			failresults.setText("Pencarian telah dibatalkan");
			
	        try{
				cancelDialog.dismiss();
		        cancelDialog = null;
	        }catch (Exception e){
	        	//nothing
	        }
		}
	        
		@Override
		protected void onPreExecute() {
			cancelDialog = new ProgressDialog(SearchActivity.this);
			cancelDialog.setMessage("Mencari lagu untuk...");
			cancelDialog.setButton(getString(R.string.btn_cancel_name), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
					retrievesearch.cancel(true);
			    }
			});
			cancelDialog.show();

			super.onPreExecute();
		} 
	        
		@Override
		protected void onPostExecute(Void result) {
			if(ia == 0 && fail == 0){
				fail = 0;
				
				adapter = new MyAdapter(SearchActivity.this, 0);
				setListAdapter(adapter);

				TextView failresults = (TextView)findViewById(R.id.failresults);
				
		        if(!isInternetConnectionActive(getApplicationContext())) {
					failresults.setText("Jika anda tidak mempunyai sambungan Internet yang aktif atau mempunya iInsyarat yang lemah. Sila menghidupkan wi-fi atau data anda dan cuba lagi.");
		        }else{
					failresults.setText("Tiada carian ditemui");
		        }
			}else if(ia == 0 && fail > 0){
				fail = 0;
				
				adapter = new MyAdapter(SearchActivity.this, 0);
				setListAdapter(adapter);

				TextView failresults = (TextView)findViewById(R.id.failresults);
				
		        if(!isInternetConnectionActive(getApplicationContext())) {
					failresults.setText("Jika anda tidak mempunyai sambungan Internet yang aktif atau mempunya iInsyarat yang lemah. Sila menghidupkan wi-fi atau data anda dan cuba lagi.");
		        }else{
					failresults.setText("Kelihatan seperti Ralat dalam pencarian. Sila cuba sekali lagi");
		        }
			}else{
				setListAdapter(adapter);
			}
			
			try{
				cancelDialog.dismiss();
				cancelDialog = null;
		    } catch (Exception e) {
		        // nothing
		    }
			
			super.onPostExecute(result);
		}
	        
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		
		public void getDilandau(String search) throws IOException, MalformedURLException, NullPointerException{
			search = search.replace(" ", "-");
			String link = "http://en.dilandau.eu/download_music/"+search+"-1.html";
			
		    // Send data
		    URL url = new URL(link);
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    conn.setConnectTimeout(5000);

			
		    // Get the response
		    StringBuffer sb = new StringBuffer();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    
		    String line;
		    while ((line = rd.readLine()) != null) {
		    	sb.append(line);
		    }
		    
		    rd.close();

			String[] a = sb.toString().split("<ul>");
			
			if(!sb.toString().contains("No songs found")){
				String[] links = a[2].split("<li class=\"arrow\">");
				
				for(int i = 1; i <= (links.length - 1); i++){
					String content = links[i];
					
					String filename = content.substring(content.indexOf("title=\"\" >"), content.indexOf("</a>"));
					filename = filename.replace("title=\"\" >", "");
					filename = filename.replaceAll("[^A-Za-z0-9 ()\\[\\]\\-]", "");
					filename = filename.replace("  ", " ");
					filename = filename.replace("  ", " ");
					
					String fileurl = content.substring(content.indexOf("href=\""), content.indexOf("\" title=\""));
					fileurl = fileurl.replace("href=\"", "");

					adapter.addAdapterItem(new AdapterItem(filename, fileurl, "dilandau", 0,  0));
					ia++;
				}
			}
		}
		
		public void getSoundcat(String search) throws IOException, MalformedURLException, NullPointerException{
			search = search.replace(" ", "%20");
			String link = "http://www.soundcat.ch/ajaxbrowse.php?q=" + search  +"&sortby=&page=1";
			Log.d("Debug", link);
		    // Send data
		    URL url = new URL(link);
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    conn.setConnectTimeout(5000);

		    // Get the response
		    StringBuffer sb = new StringBuffer();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    
		    String line;
		    while ((line = rd.readLine()) != null) {
		    	sb.append(line);
		    }
		    rd.close();

			String[] a = sb.toString().split("<div class=\"row_container");
			
			for(int i = 1; i <= (a.length - 1); i++){
				String content = a[i];
					
				String songtitle = content.substring(content.indexOf("<div class=\"listing_song_text\">"));
				songtitle = songtitle.substring(0, songtitle.indexOf("</div>"));
				songtitle = songtitle.replace("<div class=\"listing_song_text\">", "");
					
				String songartist = content.substring(content.indexOf("<div class=\"listing_artist\">"));
				songartist = songartist.substring(0, songartist.indexOf("</div>"));
				songartist = songartist.replace("<div class=\"listing_artist\">", "");
					
				String filename = songtitle + " - " + songartist;

				String fileurl = content.substring(content.indexOf("target=\"_blank\" href=\"download.php"));
				fileurl = fileurl.substring(0, fileurl.indexOf("\"></a>"));
				fileurl = fileurl.replace("target=\"_blank\" href=\"", "");

				Log.d("Debug", filename + "::" + fileurl);
				adapter.addAdapterItem(new AdapterItem(filename, "http://www.soundcat.ch/"+fileurl, "soundcat", 0,  0));
				ia++;
			}
		}
	}	 
	
	public class MyAdapter extends ArrayAdapter<AdapterItem> {
		private List<AdapterItem> items = new ArrayList<AdapterItem>();
        
		public MyAdapter(Context context, int textviewid) {
            super(context, textviewid);
            
		}
		
	    public void addAdapterItem(AdapterItem item) {
	        items.add(item);
	    }

	    @Override
		public int getCount() {
	      return items.size();
	    }

        @Override
        public AdapterItem getItem(int position) {
                return ((null != items) ? items.get(position) : null);
        }
        
	    @Override
		public long getItemId(int position) {
	            return position;
	    }

	    @Override
		public View getView(int position, View convertView, ViewGroup parent){
	      View rowView = getLayoutInflater().inflate(R.layout.searchitemview, null);
	      TextView firstTextView = (TextView) rowView.findViewById(R.id.txtTitle);
	      firstTextView.setText(items.get(position).first);	      

		  //TextView thirdTextView = (TextView) rowView.findViewById(R.id.txtSize);
		  //thirdTextView.setText("Duration: "+NumberFormat.getNumberInstance(Locale.US).format(items.get(position).fourth)+" seconds");
	      // do the same with second and third
		  
	      return rowView;
	    }
	}
	
	private boolean isInternetConnectionActive(Context context) {
	   	NetworkInfo networkInfo = ((ConnectivityManager) context
	   	    .getSystemService(Context.CONNECTIVITY_SERVICE))
	   	    .getActiveNetworkInfo();
	
	   	if(networkInfo == null || !networkInfo.isConnected()) {
	   		return false;
	   	}
		return true;
	}
	
	public String MD5(String md5) {
		   try {
		        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
		        byte[] array = md.digest(md5.getBytes());
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < array.length; ++i) {
		          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		       }
		        return sb.toString();
		    } catch (java.security.NoSuchAlgorithmException e) {
		    }
		    return null;
	}
	
	class AdapterItem {
		public String first;
		public String second;
		public String third;
		public int fourth;
		public int lid;

		public AdapterItem(String first, String second, String third, int fourth, int lid) {
			this.first = first;
			this.second = second;
			this.third = third;
			this.fourth = fourth;
			this.lid = lid;
		}
	}
	
	public String getUrl(String link) throws IOException, MalformedURLException{
		// Send data
		URL url = new URL(link);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		conn.setConnectTimeout(5000);
		// Get the response
		StringBuffer sb = new StringBuffer();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}   
		rd.close();
		
		return sb.toString();
	}
}