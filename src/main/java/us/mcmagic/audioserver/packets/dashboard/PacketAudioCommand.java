package us.mcmagic.audioserver.packets.dashboard;

import com.google.gson.JsonObject;
import us.mcmagic.audioserver.packets.BasePacket;
import us.mcmagic.audioserver.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/2/16
 */
public class PacketAudioCommand extends BasePacket {
    private UUID uuid;
    private int auth;

    public PacketAudioCommand() {
        this(null, 0);
    }

    public PacketAudioCommand(UUID uuid, int auth) {
        this.id = PacketID.Dashboard.AUDIOCOMMAND.getID();
        this.uuid = uuid;
        this.auth = auth;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getAuth() {
        return auth;
    }

    public PacketAudioCommand fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.auth = obj.get("auth").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("auth", this.auth);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
