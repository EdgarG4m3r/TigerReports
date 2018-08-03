package fr.mrtigreroux.tigerreports.listeners;

import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerCommandEvent;

import fr.mrtigreroux.tigerreports.TigerReports;
import fr.mrtigreroux.tigerreports.commands.HelpCommand;
import fr.mrtigreroux.tigerreports.data.config.ConfigFile;
import fr.mrtigreroux.tigerreports.data.constants.Permission;
import fr.mrtigreroux.tigerreports.managers.UsersManager;
import fr.mrtigreroux.tigerreports.objects.users.OfflineUser;
import fr.mrtigreroux.tigerreports.objects.users.OnlineUser;
import fr.mrtigreroux.tigerreports.objects.users.User;
import fr.mrtigreroux.tigerreports.runnables.ReportsNotifier;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;

/**
 * @author MrTigreroux
 */

public class PlayerListener implements Listener {

	private final List<String> HELP_COMMANDS = Arrays.asList("tigerreport", "helptigerreport", "reportshelp", "report?", "reports?");
	
	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		OnlineUser u = TigerReports.getInstance().getUsersManager().getOnlineUser(p);
		
		Bukkit.getScheduler().runTaskAsynchronously(TigerReports.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for(String notification : u.getNotifications())
					u.sendNotification(notification, false);
				if(Permission.STAFF.isOwned(u) && ConfigUtils.isEnabled(ConfigFile.CONFIG.get(), "Config.ReportsNotifications.Connection")) {
					String reportsNotifications = ReportsNotifier.getReportsNotification();
					if(reportsNotifications != null)
						p.sendMessage(reportsNotifications);
				}
				TigerReports.getInstance().getDb().update("REPLACE INTO tigerreports_users (uuid,name) VALUES (?,?);", Arrays.asList(p.getUniqueId().toString(), p.getName()));
			}
			
		});
		
		u.updateImmunity(Permission.REPORT_EXEMPT.isOwned(u) ? "always" : null, false);
		
		if(Permission.MANAGE.isOwned(u)) {
			String newVersion = TigerReports.getInstance().getWebManager().getNewVersion();
			if(newVersion != null) {
				boolean english = ConfigUtils.getInfoLanguage().equalsIgnoreCase("English");
				p.sendMessage("\u00A77[\u00A76TigerReports\u00A77] "+(english ? "\u00A7eThe plugin \u00A76TigerReports \u00A7ehas been updated." : "\u00A7eLe plugin \u00A76TigerReports \u00A7ea \u00E9t\u00E9 mis � jour."));
				BaseComponent updateMessage = new TextComponent(english ? "The new version \u00A77"+newVersion+" \u00A7eis available on: " : "La nouvelle version \u00A77"+newVersion+" \u00A7eest disponible ici: ");
				updateMessage.setColor(ChatColor.YELLOW);
				BaseComponent button = new TextComponent(english ? "\u00A77[\u00A7aOpen page\u00A77]" : "\u00A77[\u00A7aOuvrir la page\u00A77]");
				button.setColor(ChatColor.GREEN);
				button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder((english ? "\u00A76Left click \u00A77to open the plugin page\n\u00A77of" : "\u00A76Clic gauche \u00A77pour ouvrir la page\n\u00A77du plugin")+" \u00A7eTigerReports\u00A77.").create()));
				button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/tigerreports.25773/"));
				updateMessage.addExtra(button);
				p.spigot().sendMessage(updateMessage);
			}
		}
		
		TigerReports.getInstance().getBungeeManager().collectDelayedlyServerName();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerQuit(PlayerQuitEvent e) {
		String uuid = e.getPlayer().getUniqueId().toString();
		UsersManager userManager = TigerReports.getInstance().getUsersManager();
		User u = userManager.getSavedUser(uuid);
		if(u != null) {
			List<String> lastMessages = u.lastMessages;
			if(lastMessages.isEmpty()) {
				userManager.removeUser(uuid);
			} else {
				OfflineUser ou = new OfflineUser(uuid);
				ou.lastMessages = lastMessages;
				userManager.saveUser(ou);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	private void onPlayerLogin(PlayerLoginEvent e) {
		String error = TigerReports.getInstance().getWebManager().check(e.getPlayer().getUniqueId().toString());
		if(error != null)
			e.disallow(Result.KICK_OTHER, error);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onPlayerChat(AsyncPlayerChatEvent e) {
		TigerReports.getInstance().getUsersManager().getOnlineUser(e.getPlayer()).updateLastMessages(e.getMessage());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		String command = e.getMessage();
		if(checkHelpCommand(command.substring(1), e.getPlayer())) {
			e.setCancelled(true);
		} else if(ConfigFile.CONFIG.get().getStringList("Config.CommandsHistory").contains(command.split(" ")[0])) {
			TigerReports.getInstance().getUsersManager().getOnlineUser(e.getPlayer()).updateLastMessages(command);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onServerCommandPreprocess(ServerCommandEvent e) {
		if(checkHelpCommand(e.getCommand(), e.getSender()))
			e.setCommand("tigerreports");
	}
	
	private boolean checkHelpCommand(String command, CommandSender s) {
		if(command.equalsIgnoreCase("report help")) {
			HelpCommand.onCommand(s);
			return true;
		}
		
		command = command.toLowerCase().replace(" ", "");
		if(!command.startsWith("tigerreports:report")) {
			for(String helpCommand : HELP_COMMANDS) {
				if(command.startsWith(helpCommand)) {
					HelpCommand.onCommand(s);
					return true;
				}
			}
		}
		return false;
	}
	
}
