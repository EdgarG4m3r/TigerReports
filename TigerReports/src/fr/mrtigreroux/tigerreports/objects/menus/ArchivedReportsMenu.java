package fr.mrtigreroux.tigerreports.objects.menus;

import java.util.Arrays;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.mrtigreroux.tigerreports.TigerReports;
import fr.mrtigreroux.tigerreports.data.config.Message;
import fr.mrtigreroux.tigerreports.data.constants.MenuItem;
import fr.mrtigreroux.tigerreports.data.constants.Permission;
import fr.mrtigreroux.tigerreports.objects.Report;
import fr.mrtigreroux.tigerreports.objects.users.OnlineUser;
import fr.mrtigreroux.tigerreports.utils.ReportUtils;

/**
 * @author MrTigreroux
 */

public class ArchivedReportsMenu extends Menu implements UpdatedMenu {
	
	public ArchivedReportsMenu(OnlineUser u, int page) {
		super(u, 54, page, Permission.STAFF_ARCHIVE);
	}
	
	@Override
	protected Inventory onOpen() {
		Inventory inv = getInventory(Message.ARCHIVED_REPORTS_TITLE.get().replace("_Page_", Integer.toString(page)), true);
		
		inv.setItem(0, MenuItem.REPORTS.getWithDetails(Message.REPORTS_DETAILS.get()));
		inv.setItem(4, MenuItem.ARCHIVED_REPORTS.get());
		
		return inv;
	}
	
	@Override
	public void onUpdate(Inventory inv) {
		ReportUtils.addReports(true, inv, page, Message.REPORT_RESTORE_ACTION.get()+(Permission.STAFF_DELETE.isOwned(u) ? Message.REPORT_DELETE_ACTION.get() : ""));
	}

	@Override
	public void onClick(ItemStack item, int slot, ClickType click) {
		if(slot == 0) {
			u.openReportsMenu(1, true);
		} else if(slot >= 18 && slot <= size-9) {
			Report r = TigerReports.getInstance().getReportsManager().formatFullReport(TigerReports.getInstance().getDb().query("SELECT * FROM tigerreports_reports WHERE archived = ? LIMIT 1 OFFSET ?", Arrays.asList(1, getIndex(slot)-1)).getResult(0));
			if(r == null) {
				update(true);
			} else if(click.equals(ClickType.DROP) && Permission.STAFF_DELETE.isOwned(u)) {
				u.openConfirmationMenu(r, "DELETE_ARCHIVE");
			} else {
				r.unarchive(p.getName(), false);
				u.openDelayedlyReportsMenu();
			}
		}
	}
	
}
