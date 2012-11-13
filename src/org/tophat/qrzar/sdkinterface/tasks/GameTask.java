package org.tophat.qrzar.sdkinterface.tasks;

import org.tophat.android.exceptions.HttpException;
import org.tophat.android.mapping.Game;
import org.tophat.android.model.GameMapper;
import org.tophat.android.networking.ApiCommunicator;
import org.tophat.qrzar.sdkinterface.SdkInterface;

import android.os.AsyncTask;

/**
 * This method sends on the location from the device to the server.
 */
public class GameTask extends AsyncTask<Void, Void, Game> 
{	
	
	private ApiCommunicator apic;
	private Integer id;
	private SdkInterface sdk;
	
	public GameTask(ApiCommunicator apic, SdkInterface sdk,  Integer id)
	{
		this.apic = apic;
		this.id = id;
		this.sdk = sdk;
	}
	
	protected Game doInBackground(Void... details) 
	{
		GameMapper gm = new GameMapper(apic);
		
		Game game = null;

		try
		{
			game = (Game) gm.get(new Game(), this.id);
		}
		catch (HttpException he)
		{
			he.printStackTrace();
		}
		
		return game;
	}
	    
	protected void onPostExecute(Game game)
	{
		sdk.gameCallback(game);
	}

     @Override    
	protected void onPreExecute() 
	{       
	    super.onPreExecute();
	}
 }