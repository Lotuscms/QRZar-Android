package org.tophat.qrzar.sdkinterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.tophat.QRzar.mapper.AliveMapper;
import org.tophat.QRzar.mapper.KillMapper;
import org.tophat.QRzar.mapper.PlayerMapper;
import org.tophat.QRzar.models.Alive;
import org.tophat.QRzar.models.Kill;
import org.tophat.QRzar.models.Player;
import org.tophat.QRzar.models.TeamScore;
import org.tophat.android.exceptions.Conflict;
import org.tophat.android.exceptions.HttpException;
import org.tophat.android.mapping.Game;
import org.tophat.android.mapping.Team;
import org.tophat.android.model.ApiTokenMapper;
import org.tophat.android.networking.ApiCommunicator;
import org.tophat.qrzar.sdkinterface.tasks.GameTask;
import org.tophat.qrzar.sdkinterface.tasks.LocationTask;
import org.tophat.qrzar.sdkinterface.tasks.TeamTask;

import android.location.Location;
import android.util.Log;

public class SdkInterface 
{

	private static final String TAG = SdkInterface.class.getSimpleName();
	public static int decodeGameCode(String s){
    	return Integer.parseInt(s);
    }
	
	public static boolean isValidGameCode(String s){
    	try{
    		Integer.parseInt(s);
    		return true;
    	}catch(Exception e){
    		return false;
    	}
    }
	
	/**
	 * Static validation / parsing methods
	 */
	
	
    public static boolean isValidPlayerCode(String s){
    	if(s.length()!=6)
    		return false;
    	if(!Character.isUpperCase(s.charAt(0)))
    		return false;
   
    	return true;
    }
	
	private ApiCommunicator apic;
	
	private Integer score = 0;
	
	private Integer aliveReqs = 0;
	
	private Integer gameId;
	
	private TeamScore myteam;
	
	private Game game;
	
	private ArrayList<Team> teams;
	
	public int team1 = 0;
	public int team2 = 0;
	
	/**
	 * This variable is used to store the player location before it is updated and sent to the server.
	 */
	private Location playerLocation;
	
	
	/**
	 * Added to class for full decoupling
	 */
	
	private String mTShirtCode;
	private int mGameCode;
	private Player mPlayer;
	
	
	public SdkInterface()
	{
		apic = new ApiCommunicator(new Constants());
		
		teams = new ArrayList<Team>();
		teams.add(null);
		teams.add(null);
	}
	
	public SdkInterface(Player player)
	{
		mPlayer = player;
		apic = new ApiCommunicator(new Constants());
	}
	
	/**
	 * Test 1
	 * @return
	 */
	public void anonymous_connect() throws HttpException
	{		
		ApiTokenMapper atm = new ApiTokenMapper(apic);
		apic.setApitoken(atm.getAnonymousToken());
	}
	
	public org.tophat.QRzar.models.Player getPlayer(){
		return mPlayer;
	}
	
	public HashMap<String,Integer> getTeamScoresAndRemainingTime()
	{	
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date convertedDate = new Date();
		
		Date now = new Date();
		
		int seconds = 960;
		
		//Sometimes the game isn't quite loaded in yet, so we can use the cached version in the player object
		if ( game == null && this.mPlayer != null)
		{
			this.game = this.mPlayer.getTeam().getGame();
		}
				
		try {
			
			if (game != null)
			{
				convertedDate = dateFormat.parse(game.getEndTime());
			}
		    
		    seconds = (int) ((convertedDate.getTime()-now.getTime())/1000);;
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		
		map.put("timer", seconds);
		return map;
	}
	
	/**
     * This method checks if the code is a valid respawn code for the server.
     * @param result
     * @return
     */
	public boolean isValidRespawnCode(String result) 
	{
		if(result.length() == 6 && result.charAt(0) == 'R' && result.charAt(1) == 'E')
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean joinGame(){
		if(mGameCode==0||!isValidPlayerCode(mTShirtCode))
				return false;
    	try 
    	{
			mPlayer = this.joinGame(mTShirtCode, mGameCode);
			return true;
    	}
    	catch (Conflict c)
    	{
    		c.printStackTrace();
    		System.err.println("This player is already in game.");
    		return false;
		} 
		catch (HttpException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    }
	
	/**
	 * Game the game.
	 */
	public Player joinGame(String qrCode, Integer gameId) throws HttpException
	{
		  Game g = new Game();
		  this.gameId = gameId;
		  g.setId(gameId);
		  
		  //Get the game details.
		  new GameTask(apic, this, g.getId()).execute();
		  
		  Player p = new Player();
		  p.setGame(g);
		  p.setName("I'm the best");
		  p.setQrcode(qrCode);
		  
		  System.err.println("JOIN GAME");
		  
		  PlayerMapper pm = new PlayerMapper(apic);

		  //Create the new player.
		  return (Player)pm.create(p);
	}
	
	/**
	 * This method provides direct access to the kill request of the server.
	 * @param killer
	 * @param victimCode
	 * @throws HttpException
	 */
	public void kill(Player killer, String victimCode) throws HttpException
	{
		  Kill k = new Kill();
		  k.setKiller(killer);
		  k.setVictimQrcode(victimCode);
		  
		  KillMapper km = new KillMapper(apic);

		  km.create(k); 
	}
	
	public void calculateScores()
	{
		team1 = 0;
		team2 = 0;
		
		if ( teams.get(0) != null)
		{
			System.err.println("PROCESSING 0:");
			for ( org.tophat.android.mapping.Player p : teams.get(0).getPlayers())
			{
				team1 += p.getScore();
			}
		}
		
		if ( teams.get(1) != null )
		{
			System.err.println("PROCESSING 1:");
			for ( org.tophat.android.mapping.Player p : teams.get(1).getPlayers())
			{
				team2 += p.getScore();
			}
		}
		
		System.err.println("TEAM 1:"+team1);
		System.err.println("TEAM 2:"+team2);
	}
	
	/**
	 * Player is alive
	 * @return
	 */
	public boolean playerIsAlive()
	{
		//AliveMapper am = new AliveMapper(this.apic);
		
		//Alive alive = new Alive();
		//alive.setId(mPlayer.getId());
		
		//try {
		//	alive = (Alive)am.get(alive);
		//} catch (HttpException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		this.aliveReqs++;
		
		new TeamTask(apic, this, this.mPlayer.getTeam().getId()).execute();
		
		if(aliveReqs >= 5)
		{
			new LocationTask(apic, this, this.playerLocation).execute();
			
			for (Team t : this.mPlayer.getTeam().getGame().getTeams())
			{
				if ( t.getId() != this.mPlayer.getTeam().getId())
				{
					new TeamTask(apic, this, t.getId()).execute();
				}
			}
			
			aliveReqs = 0;
		}	
			
		
		if (teams.get(0) != null)
		{
			System.err.println("PROCESSING 0:");
			for ( org.tophat.android.mapping.Player p : teams.get(0).getPlayers())
			{
				if (p.getId() == this.mPlayer.getId() && p.getAlive())
				{
					Log.i(TAG, "Alive: True");
					return true;
				}
			}
		}
		else
		{
			Log.i(TAG, "Alive: True but fake");
			return true;
		}
		
		Log.i(TAG, "Alive: False");
		return false;
	}
    
    /**
	 * Respawn method
	 * @throws HttpException 
	 */
	public void respawn(Player me, String respawnCode) throws HttpException
	{	  
		PlayerMapper pm = new PlayerMapper(apic);
		
		//The method name is weird because of constraints added by the Jackson - the JSON library.
		me.setRespawn_code(respawnCode);
		
		//Just to ensure that the players URL is used correctly with this program.
		me.setAccessUrl("players");
		
		pm.update(me);
	}
    
    public void setGameCode(int gameCode)
    {
		mGameCode = gameCode;
	}

    public void setTShirtCode(String tShirtCode)
    {
		mTShirtCode = tShirtCode;
	}

    /**
	 * 
	 * @param id
	 * @param team
	 */
	public void teamCallback(Integer id, Team team) 
	{
		if ( id == this.mPlayer.getTeam().getId())
			teams.set(0, team);
		else
			teams.set(1, team);
		calculateScores();
	}
    
    /**
     * This method is called in the game activity when a new location of a player is made available. 
     * This will then be transfered to the server at the next allotted data transfer.
     * @param location
     */
	public void updateLocation(Location location) 
	{
		this.playerLocation = location;
	}

    public boolean validToProcessGameCode(String s)
    {
		return isValidGameCode(s)&&mTShirtCode!=null;
	}

	public boolean validToProcessTShirt(String s)
	{
		return isValidPlayerCode(s)&&mTShirtCode==null;
	}

	public void gameCallback(Game game) 
	{
		this.game = game;
	}
}
