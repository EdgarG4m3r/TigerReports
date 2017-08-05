package fr.mrtigreroux.tigerreports.listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import fr.mrtigreroux.tigerreports.TigerReports;
import fr.mrtigreroux.tigerreports.commands.HelpCommand;
import fr.mrtigreroux.tigerreports.data.config.ConfigFile;
import fr.mrtigreroux.tigerreports.data.constants.Permission;
import fr.mrtigreroux.tigerreports.objects.users.OnlineUser;
import fr.mrtigreroux.tigerreports.runnables.ReportsNotifier;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.UserUtils;

/**
 * @author MrTigreroux
 */

public class PlayerListener implements Listener {

	private final static Set<String> helpCommands = new HashSet<>(Arrays.asList("/tigerreports", "/helptigerreports", "/helptigerreport", "/tigerreport", "/tigerreporthelp", "/tigerreportshelp"));
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		OnlineUser u = UserUtils.getOnlineUser(p);
		for(String notification : u.getNotifications()) u.sendNotification(notification, false);
		if(Permission.STAFF.isOwned(u) && ConfigUtils.isEnabled(ConfigFile.CONFIG.get(), "Config.ReportsNotifications.Connection")) {
			String reportsNotifications = ReportsNotifier.getReportsNotification();
			if(reportsNotifications != null) p.sendMessage(reportsNotifications);
		}
		
		TigerReports.getDb().updateAsynchronously("REPLACE INTO users (uuid,name) VALUES (?,?);", Arrays.asList(p.getUniqueId().toString(), p.getName()));
		u.updateImmunity(Permission.EXEMPT.isOwned(u) ? "always" : null, false);
		
		if(Permission.MANAGE.isOwned(u)) {
			String newVersion = TigerReports.getNewVersion();
			if(newVersion != null) {
				boolean english = ConfigUtils.getInfoLanguage().equalsIgnoreCase("English");
				p.sendMessage(english ? "�7[�6TigerReports�7] �eThe plugin �6TigerReports �ehas been updated." : "�7[�6TigerReports�7] �eLe plugin �6TigerReports �ea �t� mis � jour.");
				BaseComponent updateMessage = new TextComponent(english ? "New version �7"+newVersion+" �eis available on: " : "La nouvelle version �7"+newVersion+" �eest disponible ici: ");
				updateMessage.setColor(ChatColor.YELLOW);
				BaseComponent button = new TextComponent(english ? "�7[�aOpen page�7]" : "�7[�aOuvrir la page�7]");
				button.setColor(ChatColor.GREEN);
				button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(english ? "�6Left click �7to open plugin page\n�7of �eTigerReports�7." : "�6Clic gauche �7pour ouvrir la page\n�7du plugin �eTigerReports�7.").create()));
				button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/tigerreports.25773/"));
				updateMessage.addExtra(button);
				p.spigot().sendMessage(updateMessage);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		String uuid = e.getPlayer().getUniqueId().toString();
		if(TigerReports.Users.containsKey(uuid) && TigerReports.Users.get(uuid).getLastMessages() == null) TigerReports.Users.remove(uuid);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		UserUtils.getOnlineUser(e.getPlayer()).updateLastMessages(e.getMessage());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if(checkHelpCommand(e.getMessage(), e.getPlayer())) e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerCommandPreprocess(ServerCommandEvent e) {
		checkHelpCommand("/"+e.getCommand(), e.getSender());
	}
	
	private boolean checkHelpCommand(String command, CommandSender s) {
		command = command.replace(" ", "");
		for(String helpCommand : helpCommands) {
			if(command.startsWith(helpCommand)) {
				HelpCommand.onCommand(s);
				return true;
			}
		}
		return false;
	}
	
}
