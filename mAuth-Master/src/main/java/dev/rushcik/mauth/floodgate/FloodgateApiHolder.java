package dev.rushcik.mauth.floodgate;

import java.util.UUID;
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * Holder class for optional floodgate feature, we can't inject of optional plugins without holders due to Velocity structure.
 */
public class FloodgateApiHolder {

  private final FloodgateApi floodgateApi;

  public FloodgateApiHolder() {
    this.floodgateApi = FloodgateApi.getInstance();
  }

  public boolean isFloodgatePlayer(UUID uuid) {
    return this.floodgateApi.isFloodgatePlayer(uuid);
  }

  public int getPrefixLength() {
    return this.floodgateApi.getPlayerPrefix().length();
  }
}
