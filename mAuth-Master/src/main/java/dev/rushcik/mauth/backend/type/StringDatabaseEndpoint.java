package dev.rushcik.mauth.backend.type;

import java.util.function.Function;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.handler.AuthSessionHandler;
import dev.rushcik.mauth.model.RegisteredPlayer;

public class StringDatabaseEndpoint extends StringEndpoint {

  public StringDatabaseEndpoint(mAuth plugin, String type, String username, String value) {
    super(plugin, type, username, value);
  }

  public StringDatabaseEndpoint(mAuth plugin, String type, Function<RegisteredPlayer, String> function) {
    super(plugin, type, username -> {
      RegisteredPlayer player = AuthSessionHandler.fetchInfo(plugin.getPlayerDao(), username);
      if (player == null) {
        return "";
      } else {
        return function.apply(player);
      }
    });
  }
}
