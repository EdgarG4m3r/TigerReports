package fr.mrtigreroux.tigerreports.data.constants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.mrtigreroux.tigerreports.data.config.Message;
import fr.mrtigreroux.tigerreports.objects.CustomItem;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;
import fr.mrtigreroux.tigerreports.utils.VersionUtils;

/**
 * @author MrTigreroux
 */

public enum MenuItem {

	CLOSE(VersionUtils.isOldVersion() ? Material.REDSTONE_BLOCK : Material.BARRIER, Message.CLOSE),
	PAGE_SWITCH_PREVIOUS(Material.FEATHER, Message.PAGE_SWITCH_PREVIOUS),
	PAGE_SWITCH_NEXT(Material.FEATHER, Message.PAGE_SWITCH_NEXT),

	REASONS(Material.BOOK, Message.REASONS),
	PUNISHMENTS(Material.GOLD_AXE, Message.PUNISHMENTS, true),
	REPORTS(Material.BOOKSHELF, Message.REPORTS),
	ARCHIVED_REPORTS(Material.BOOKSHELF, Message.ARCHIVED_REPORTS),
	PUNISH_ABUSE(Material.GOLD_AXE, Message.PUNISH_ABUSE, true),
	DATA(Material.ENCHANTED_BOOK, Message.DATA),
	ARCHIVE(Material.STAINED_CLAY, (short) 9, Message.ARCHIVE, Message.ARCHIVE_DETAILS.get(), false),
	DELETE(Material.FLINT_AND_STEEL, Message.DELETE, Message.DELETE_DETAILS.get(), false),
	COMMENTS(Material.WRITTEN_BOOK, Message.COMMENTS, true),
	CANCEL_PROCESS(Material.FEATHER, Message.CANCEL_PROCESS, Message.CANCEL_PROCESS_DETAILS.get(), false),
	WRITE_COMMENT(Material.BOOK_AND_QUILL, Message.WRITE_COMMENT, Message.WRITE_COMMENT_DETAILS.get(), false);

	private final Material material;
	private final short durability;
	private final Message name;
	private final String details;
	private final boolean hideFlags;

	MenuItem(Material material, Message name) {
		this(material, name, null, false);
	}

	MenuItem(Material material, Message name, boolean hideFlags) {
		this(material, name, null, hideFlags);
	}

	MenuItem(Material material, Message name, String details, boolean hideFlags) {
		this(material, (short) 0, name, details, hideFlags);
	}

	MenuItem(Material material, short durability, Message name, String details, boolean hideFlags) {
		this.material = material;
		this.durability = durability;
		this.name = name;
		this.details = details;
		this.hideFlags = hideFlags;
	}

	public ItemStack getWithDetails(String details) {
		return new CustomItem().type(material)
				.damage(durability)
				.name(name.get())
				.lore(details != null ? details.split(ConfigUtils.getLineBreakSymbol()) : null)
				.hideFlags(hideFlags)
				.create();
	}

	public ItemStack get() {
		return getWithDetails(details);
	}

}
