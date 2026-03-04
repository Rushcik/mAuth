package dev.rushcik.mauth.command;

import com.velocitypowered.api.command.CommandSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import net.elytrium.commons.kyori.serialization.Serializer;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.Settings;
import dev.rushcik.mauth.handler.AuthSessionHandler;
import net.kyori.adventure.text.Component;

public class ForceLoginCommand extends RatelimitedCommand {

  private final mAuth plugin;

  private final String successful;
  private final String unknownPlayer;
  private final Component usage;

  public ForceLoginCommand(mAuth plugin) {
    this.plugin = plugin;

    this.successful = Settings.IMP.MAIN.STRINGS.FORCE_LOGIN_SUCCESSFUL;
    this.unknownPlayer = Settings.IMP.MAIN.STRINGS.FORCE_LOGIN_UNKNOWN_PLAYER;
    this.usage = mAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_LOGIN_USAGE);
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    if (args.length == 1) {
      String nickname = args[0];

      Serializer serializer = mAuth.getSerializer();
      AuthSessionHandler handler = this.plugin.getAuthenticatingPlayer(nickname);
      if (handler == null) {
        source.sendMessage(serializer.deserialize(MessageFormat.format(this.unknownPlayer, nickname)));
        return;
      }

      handler.finishLogin();
      source.sendMessage(serializer.deserialize(MessageFormat.format(this.successful, nickname)));
    } else {
      source.sendMessage(this.usage);
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.FORCE_LOGIN
        .hasPermission(invocation.source(), "mAuth.admin.forcelogin");
  }

  @Override
  public List<String> suggest(Invocation invocation) {
    if (invocation.arguments().length > 1) {
      return super.suggest(invocation);
    }

    String nickname = invocation.arguments().length == 0 ? "" : invocation.arguments()[0];
    List<String> suggest = new ArrayList<>();
    for (String username : this.plugin.getAuthenticatingPlayers().keySet()) {
      if (username.startsWith(nickname)) {
        suggest.add(username);
      }
    }

    return suggest;
  }
}
