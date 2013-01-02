package me.botsko.prism.appliers;

import java.util.ArrayList;

import me.botsko.prism.actionlibs.QueryParameters;

import org.bukkit.entity.Player;

public class PreviewSession {
	
	/**
	 * 
	 */
	protected ArrayList<Undo> undo_queue = new ArrayList<Undo>();
	
	/**
	 * 
	 */
	protected Player player;
	
	/**
	 * 
	 */
	protected QueryParameters parameters;
	
	
	/**
	 * 
	 * @param player
	 * @param undo
	 * @param args
	 */
	public PreviewSession( Player player, ArrayList<Undo> undo, QueryParameters parameters ){
		this.undo_queue = undo;
		this.player = player;
		this.parameters = parameters;
	}


	/**
	 * @return the undo_queue
	 */
	public ArrayList<Undo> getUndo_queue() {
		return undo_queue;
	}


	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}


	/**
	 * @return the args
	 */
	public QueryParameters getArgs() {
		return parameters;
	}
}