package org.tophat.qrzar.sdkinterface.tasks;

import org.tophat.android.exceptions.HttpException;
import org.tophat.android.exceptions.NoInternetConnection;
import org.tophat.android.exceptions.NotFound;
import org.tophat.android.mapping.Team;
import org.tophat.android.model.TeamMapper;
import org.tophat.android.networking.ApiCommunicator;
import org.tophat.qrzar.sdkinterface.SdkInterface;

import android.os.AsyncTask;

/**
 * This method sends on the location from the device to the server.
 */
public class TeamTask extends AsyncTask<Void, Void, Team> 
{	
	
	private ApiCommunicator apic;
	private Integer id;
	private SdkInterface sdk;
	
	public TeamTask(ApiCommunicator apic, SdkInterface sdk,  Integer id)
	{
		this.apic = apic;
		this.id = id;
		this.sdk = sdk;
	}
	
	@Override    
	protected void onPreExecute() 
	{       
	    super.onPreExecute();
	}
	    
	protected Team doInBackground(Void... details) 
	{
		TeamMapper tsm = new TeamMapper(apic);
		
		Team team = null;
		
		try 
		{
			team = tsm.get(id);
		} 
		catch (NotFound nf)
		{
			nf.printStackTrace();
		}
		catch (NoInternetConnection nic)
		{
			nic.printStackTrace();
			
			//Special no Internet connection data handling.
		}
		catch (HttpException e) 
		{
			e.printStackTrace();
		}
		
		return team;
	}

     protected void onPostExecute(Team team)
     {
    	 sdk.teamCallback(this.id, team);
     }
 }