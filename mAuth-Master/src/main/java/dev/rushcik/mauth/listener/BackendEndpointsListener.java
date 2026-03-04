package dev.rushcik.mauth.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import io.netty.buffer.Unpooled;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.function.Function;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.Settings;
import dev.rushcik.mauth.backend.Endpoint;
import dev.rushcik.mauth.backend.type.LongDatabaseEndpoint;
import dev.rushcik.mauth.backend.type.StringDatabaseEndpoint;
import dev.rushcik.mauth.backend.type.StringEndpoint;
import dev.rushcik.mauth.backend.type.UnknownEndpoint;
import dev.rushcik.mauth.model.RegisteredPlayer;

public class BackendEndpointsListener {

  public static final ChannelIdentifier API_CHANNEL = MinecraftChannelIdentifier.create("mAuth", "backend_api");

  public static final Map<String, Function<mAuth, Endpoint>> TYPES = Map.ofEntries(
      new SimpleEntry<>("available_endpoints", plugin -> new StringEndpoint(plugin, "available_endpoints",
          username -> String.join(",", Settings.IMP.MAIN.BACKEND_API.ENABLED_ENDPOINTS))),
      new SimpleEntry<>("premium_state", lauth -> new StringEndpoint(lauth, "premium_state",
          username -> lauth.isPremiumInternal(username).getState().name())),
      new SimpleEntry<>("hash", plugin -> new StringDatabaseEndpoint(plugin, "hash", RegisteredPlayer::getHash)),
      new SimpleEntry<>("totp_token", plugin -> new StringDatabaseEndpoint(plugin, "totp_token", RegisteredPlayer::getTotpToken)),
      new SimpleEntry<>("reg_date", plugin -> new LongDatabaseEndpoint(plugin, "reg_date", RegisteredPlayer::getRegDate)),
      new SimpleEntry<>("uuid", plugin -> new StringDatabaseEndpoint(plugin, "uuid", RegisteredPlayer::getUuid)),
      new SimpleEntry<>("premium_uuid", plugin -> new StringDatabaseEndpoint(plugin, "premium_uuid", RegisteredPlayer::getPremiumUuid)),
      new SimpleEntry<>("ip", plugin -> new StringDatabaseEndpoint(plugin, "ip", RegisteredPlayer::getIP)),
      new SimpleEntry<>("login_ip", plugin -> new StringDatabaseEndpoint(plugin, "login_ip", RegisteredPlayer::getLoginIp)),
      new SimpleEntry<>("login_date", plugin -> new LongDatabaseEndpoint(plugin, "login_date", RegisteredPlayer::getLoginDate)),
      new SimpleEntry<>("token_issued_at", plugin -> new LongDatabaseEndpoint(plugin, "token_issued_at", RegisteredPlayer::getTokenIssuedAt))
  );

  private final mAuth plugin;

  public BackendEndpointsListener(mAuth plugin) {
    this.plugin = plugin;

    plugin.getServer().getChannelRegistrar().register(API_CHANNEL);
  }

  @Subscribe
  public void onRequest(PluginMessageEvent event) {
    if (event.getIdentifier() != API_CHANNEL) {
      return;
    }

    event.setResult(ForwardResult.handled());
    if (!(event.getSource() instanceof VelocityServerConnection server) || !server.isActive()) {
      return;
    }

    Endpoint endpoint;
    ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
    String dataType = in.readUTF();
    Function<mAuth, Endpoint> typeFunc = TYPES.get(dataType);
    if (typeFunc == null) {
      endpoint = new UnknownEndpoint(this.plugin, dataType);
    } else {
      endpoint = typeFunc.apply(this.plugin);
      endpoint.read(in);
    }

    MinecraftConnection connection = server.getConnection();
    if (connection != null && !connection.isClosed()) {
      ByteArrayDataOutput output = ByteStreams.newDataOutput();
      endpoint.write(output);
      connection.write(new PluginMessagePacket(API_CHANNEL.getId(), Unpooled.wrappedBuffer(output.toByteArray())));
    }
  }
}
