package dev.rushcik.mauth.backend.type;

import java.util.function.Function;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.handler.AuthSessionHandler;
import dev.rushcik.mauth.model.RegisteredPlayer;

public class LongDatabaseEndpoint extends LongEndpoint {

  public LongDatabaseEndpoint(mAuth plugin, String type, String username, long value) {
    super(plugin, type, username, value);
  }

  public LongDatabaseEndpoint(mAuth plugin, String type, Function<RegisteredPlayer, Long> function) {
    super(plugin, type, username -> {
      RegisteredPlayer player = AuthSessionHandler.fetchInfo(plugin.getPlayerDao(), username);
      if (player == null) {
        return Long.MIN_VALUE;
      } else {
        return function.apply(player);
      }
    });
  }
}
