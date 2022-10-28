package us.mcmagic.audioserver.packets.dashboard;

import com.google.gson.JsonObject;
import us.mcmagic.audioserver.packets.BasePacket;
import us.mcmagic.audioserver.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 8/22/16
 */
public class PacketFriendRequest extends BasePacket {
    private UUID uuid;
    private String from;

    public PacketFriendRequest() {
        this(null, "");
    }

    public PacketFriendRequest(UUID uuid, String from) {
        this.id = PacketID.Dashboard.FRIENDREQUEST.getID();
        this.uuid = uuid;
        this.from = from;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getFrom() {
        return from;
    }

    public PacketFriendRequest fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.from = obj.get("from").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("from", this.from);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}