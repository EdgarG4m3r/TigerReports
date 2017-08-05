package fr.mrtigreroux.tigerreports.objects.menus;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.mrtigreroux.tigerreports.data.config.Message;
import fr.mrtigreroux.tigerreports.data.constants.Permission;
import fr.mrtigreroux.tigerreports.data.constants.Statistic;
import fr.mrtigreroux.tigerreports.objects.CustomItem;
import fr.mrtigreroux.tigerreports.objects.users.OnlineUser;
import fr.mrtigreroux.tigerreports.objects.users.User;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;

/**
 * @author MrTigreroux
 */

public class UserMenu extends Menu implements UpdatedMenu {
	
	public UserMenu(OnlineUser u, User tu) {
		super(u, 54, 0, Permission.STAFF, -1, null, tu);
	}
	
	@Override
	public Inventory onOpen() {
		String name = tu.getName();
		Inventory inv = getInventory(Message.USER_TITLE.get().replace("_Target_", name), true);
		
		inv.setItem(4, new CustomItem().skullOwner(name).name(Message.USER.get().replace("_Target_", name)).create());
		
		return inv;
	}

	@Override
	public void onUpdate(Inventory inv) {
		if(!check()) return;
		
		String cooldown = tu.getCooldown();
		inv.setItem(8, new CustomItem().type(Material.GOLD_AXE).hideFlags(true).name(Message.COOLDOWN_STATUS.get().replace("_Time_", cooldown != null ? cooldown : Message.NONE_FEMALE.get()))
				.lore(cooldown != null ? Message.COOLDOWN_STATUS_DETAILS.get().replace("_Player_", tu.getName()).split(ConfigUtils.getLineBreakSymbol()) : null).create());
		
		Map<String, Integer> statistics = tu.getStatistics();
		for(Statistic stat : Statistic.values()) {
			int value = statistics.get(stat.getConfigName());
			inv.setItem(stat.getPosition(), stat.getItem().amount(value).name(Message.USER_STATISTIC.get().replace("_Statistic_", stat.getName()).replace("_Amount_", ""+value))
					.lore(Permission.ADVANCED.isOwned(u) ? Message.USER_STATISTIC_DETAILS.get().split(ConfigUtils.getLineBreakSymbol()) : null).create());
		}
	}
	
	@Override
	public void onClick(ItemStack item, int slot, ClickType click) {
		if(slot == 8) {
			if(tu.getCooldown() != null) {
				tu.stopCooldown(p.getName(), false);
				open(false);
			}
		} else if(slot != 4 && Permission.ADVANCED.isOwned(u)) {
			String stat = "";
			for(Statistic statistics : Statistic.values()) if(statistics.getPosition() == slot) stat = statistics.getConfigName();
			tu.changeStatistic(stat, click.toString().contains("RIGHT") ? -1 : 1, false);
			open(true);
		}
	}
	
}
