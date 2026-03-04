package dev.rushcik.mauth.event;

import com.velocitypowered.api.proxy.Player;
import java.util.function.Consumer;
import dev.rushcik.mauth.model.RegisteredPlayer;

public class PreAuthorizationEvent extends PreEvent {

  private final RegisteredPlayer playerInfo;

  public PreAuthorizationEvent(Consumer<TaskEvent> onComplete, Result result, Player player, RegisteredPlayer playerInfo) {
    super(onComplete, result, player);

    this.playerInfo = playerInfo;
  }

  public RegisteredPlayer getPlayerInfo() {
    return this.playerInfo;
  }
}
