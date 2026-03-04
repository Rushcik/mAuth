package dev.rushcik.mauth.event;

import java.util.function.Consumer;
import net.elytrium.limboapi.api.player.LimboPlayer;
import dev.rushcik.mauth.model.RegisteredPlayer;

public class PostAuthorizationEvent extends PostEvent {

  public PostAuthorizationEvent(Consumer<TaskEvent> onComplete, LimboPlayer player, RegisteredPlayer playerInfo, String password) {
    super(onComplete, player, playerInfo, password);
  }
}
