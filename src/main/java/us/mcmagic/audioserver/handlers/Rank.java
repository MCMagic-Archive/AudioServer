package us.mcmagic.audioserver.handlers;

/**
 * Created by Marc on 7/15/16
 */
public enum Rank {
    OWNER("Owner", ChatColor.GOLD, ChatColor.YELLOW, 10),
    MAYOR("Mayor", ChatColor.GOLD, ChatColor.YELLOW, 10),
    MANAGER("Manager", ChatColor.GOLD, ChatColor.YELLOW, 10),
    DEVELOPER("Developer", ChatColor.GOLD, ChatColor.YELLOW, 10),
    COORDINATOR("Coordinator", ChatColor.GREEN, ChatColor.GREEN, 9),
    CASTMEMBER("Cast Member", ChatColor.GREEN, ChatColor.GREEN, 8),
    EARNINGMYEARS("Earning My Ears", ChatColor.GREEN, ChatColor.GREEN, 7),
    CHARACTER("Character", ChatColor.BLUE, ChatColor.BLUE, 6),
    CHARACTERGUEST("Character", ChatColor.AQUA, ChatColor.BLUE, 5),
    SPECIALGUEST("Special Guest", ChatColor.DARK_PURPLE, ChatColor.WHITE, 4),
    MCPROHOSTING("MCProHosting", ChatColor.RED, ChatColor.WHITE, 4),
    MINEDISNEY("MineDisney", ChatColor.DARK_PURPLE, ChatColor.WHITE, 4),
    CRAFTVENTURE("Craftventure", ChatColor.DARK_PURPLE, ChatColor.WHITE, 4),
    MAGICALDREAMS("MagicalDreams", ChatColor.DARK_PURPLE, ChatColor.WHITE, 4),
    ADVENTURERIDGE("AdventureRidge", ChatColor.DARK_PURPLE, ChatColor.WHITE, 4),
    ANCHORNETWORK("AnchorNetwork", ChatColor.DARK_PURPLE, ChatColor.WHITE, 4),
    SHAREHOLDER("Shareholder", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, 3),
    DVCMEMBER("DVC", ChatColor.AQUA, ChatColor.WHITE, 2),
    GUEST("Guest", ChatColor.DARK_AQUA, ChatColor.WHITE, 1);

    private String name;
    private ChatColor tagColor;
    private ChatColor chatColor;
    private int rankId;

    Rank(String name, ChatColor tagColor, ChatColor chatColor, int rankId) {
        this.name = name;
        this.tagColor = tagColor;
        this.chatColor = chatColor;
        this.rankId = rankId;
    }

    public int getRankId() {
        return rankId;
    }

    public static Rank fromString(String string) {
        String rankName = string.toLowerCase();
        switch (rankName) {
            case "owner":
                return OWNER;
            case "mayor":
                return MAYOR;
            case "manager":
                return MANAGER;
            case "developer":
                return DEVELOPER;
            case "technician":
                return DEVELOPER;
            case "moderator":
                return CASTMEMBER;
            case "coordinator":
                return COORDINATOR;
            case "castmember":
                return CASTMEMBER;
            case "earningmyears":
                return EARNINGMYEARS;
            case "character":
                return CHARACTER;
            case "characterguest":
                return CHARACTERGUEST;
            case "specialguest":
                return SPECIALGUEST;
            case "mcprohosting":
                return MCPROHOSTING;
            case "craftventure":
                return CRAFTVENTURE;
            case "minedisney":
                return MINEDISNEY;
            case "magicaldreams":
                return MAGICALDREAMS;
            case "adventureridge":
                return ADVENTURERIDGE;
            case "anchornetwork":
                return ANCHORNETWORK;
            case "shareholder":
                return SHAREHOLDER;
            case "dvc":
                return DVCMEMBER;
            case "donor":
                return DVCMEMBER;
            case "dvcmember":
                return DVCMEMBER;
            case "newplayer":
                return GUEST;
            case "guest":
                return GUEST;
            default:
                return GUEST;
        }
    }

    public String getName() {
        return name;
    }

    public String getSqlName() {
        if (this.equals(Rank.CHARACTERGUEST)) {
            return "characterguest";
        } else {
            return name.toLowerCase().replaceAll(" ", "");
        }
    }

    public String getNameWithBrackets() {
        return ChatColor.WHITE + "[" + getTagColor() + getName() + ChatColor.WHITE + "]";
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public ChatColor getTagColor() {
        return tagColor;
    }
}