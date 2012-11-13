package org.tophat.qrzar.sdkinterface.tasks;

import org.tophat.QRzar.mapper.PlayerMapper;
import org.tophat.QRzar.models.Player;
import org.tophat.android.exceptions.HttpException;
import org.tophat.android.networking.ApiCommunicator;
import org.tophat.qrzar.sdkinterface.SdkInterface;

import android.location.Location;
import android.os.AsyncTask;

/**
 * This method sends on the location from the device to the server.
 */
public class LocationTask extends AsyncTask<Void, Void, Void> 
{	
	
	private ApiCommunicator apic;
	private Location l;
	private SdkInterface sdk;
	
	public LocationTask(ApiCommunicator apic, SdkInterface sdk,  Location l)
	{
		this.apic = apic;
		this.l = l;
		this.sdk = sdk;
	}
	
	protected Void doInBackground(Void... details) 
	{
		PlayerMapper pm = new PlayerMapper(apic);
		
		Player p = sdk.getPlayer();
		
		if (p != null && l != null)
		{
			p.setAccessUrl("players");
			p.setLatitude(l.getLatitude());
			p.setLongitude(l.getLongitude());
			
			try
			{
				pm.update(p);
			}
			catch (HttpException he)
			{
				he.printStackTrace();
			}
		}
		
		return null;
	}
	    
	protected void onPostExecute(Void data)
     {
     }

     @Override    
	protected void onPreExecute() 
	{       
	    super.onPreExecute();
	}
 }