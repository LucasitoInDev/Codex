package cx.ajneb97.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import cx.ajneb97.Codex;
import cx.ajneb97.data.AgregarEntradaCallback;
import cx.ajneb97.data.JugadorCodex;
import cx.ajneb97.data.JugadorCodexCallback;
import cx.ajneb97.data.MySQL;

public class JugadorDataManager {
	
	//Cuando MySQL esta activado no deberia usar esta lista
	private ArrayList<JugadorCodex> jugadores;
	private Codex plugin;
	
	public JugadorDataManager(Codex plugin) {
		this.jugadores = new ArrayList<JugadorCodex>();
		this.plugin = plugin;
	}
	
	public ArrayList<JugadorCodex> getJugadores() {
		return jugadores;
	}

	public void setJugadores(ArrayList<JugadorCodex> jugadores) {
		this.jugadores = jugadores;
	}
	
	public void agregarJugador(JugadorCodex j) {
		jugadores.add(j);
	}
	
	public JugadorCodex getJugadorSync(String nombre) {
		JugadorCodex jugadorCodex = null;
		if(MySQL.isEnabled(plugin.getConfig())) {
			jugadorCodex = MySQL.getJugador(nombre, plugin);
		}else {
			for(JugadorCodex j : jugadores) {
				if(j.getName() != null && j.getName().equals(nombre)) {
					jugadorCodex = j;
					break;
				}
			}
		}
		return jugadorCodex;
	}
	
	public void getJugador(String nombre,JugadorCodexCallback callback) {
		new BukkitRunnable() {
			@Override
			public void run() {
				JugadorCodex jugadorCodex = getJugadorSync(nombre);

				new BukkitRunnable() {
					@Override
					public void run() {
						callback.onDone(jugadorCodex);
					}
					
				}.runTask(plugin);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	public void agregarEntrada(Player jugador,String categoria,String discovery,AgregarEntradaCallback callback) {
		getJugador(jugador.getName(),new JugadorCodexCallback() {
			@Override
			public void onDone(JugadorCodex j) {
				boolean agrega = false;
				if(MySQL.isEnabled(plugin.getConfig())) {
					if(j == null) {
						j = new JugadorCodex(jugador.getUniqueId().toString(),jugador.getName());
						j.agregarEntrada(categoria, discovery);
						agrega = true;
						MySQL.actualizarDiscoveriesJugador(plugin, j);
					}else {
						agrega = j.agregarEntrada(categoria, discovery);
						if(agrega) {
							MySQL.actualizarDiscoveriesJugador(plugin, j);
						}
					}
				}else {
					if(j == null) {
						//Lo crea
						j = new JugadorCodex(jugador.getUniqueId().toString(),jugador.getName());
						agregarJugador(j);
					}
					
					agrega = j.agregarEntrada(categoria, discovery);
				}
				
				boolean agregaFinal = agrega;
				
				new BukkitRunnable() {
					@Override
					public void run() {
						callback.onDone(agregaFinal);
					}
					
				}.runTask(plugin);
			}
		});
	}
	
	public void resetearEntrada(JugadorCodex j,String categoria,String discovery,boolean todas) {
		if(MySQL.isEnabled(plugin.getConfig())) {
			if(todas) {
				j.resetearEntradas();
			}else {
				j.resetearEntrada(categoria, discovery);
			}
			MySQL.actualizarDiscoveriesJugador(plugin, j);
		}else {
			if(todas) {
				j.resetearEntradas();
			}else {
				j.resetearEntrada(categoria, discovery);
			}
			
		}
	}

}
