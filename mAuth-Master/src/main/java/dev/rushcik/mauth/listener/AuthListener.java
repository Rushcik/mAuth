package dev.rushcik.mauth.listener;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.util.UuidUtils;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.elytrium.commons.utils.reflection.ReflectionException;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.mAuth.CachedPremiumUser;
import dev.rushcik.mauth.mAuth.PremiumState;
import dev.rushcik.mauth.Settings;
import dev.rushcik.mauth.floodgate.FloodgateApiHolder;
import dev.rushcik.mauth.handler.AuthSessionHandler;
import dev.rushcik.mauth.model.RegisteredPlayer;
import dev.rushcik.mauth.model.SQLRuntimeException;
import net.kyori.adventure.text.Component;

public class AuthListener {

  private static final MethodHandle DELEGATE_FIELD;

  private final mAuth plugin;
  private final Dao<RegisteredPlayer, String> playerDao;
  private final FloodgateApiHolder floodgateApi;
  private final Component errorOccurred;

  public AuthListener(mAuth plugin, Dao<RegisteredPlayer, String> playerDao, FloodgateApiHolder floodgateApi) {
    this.plugin = plugin;
    this.playerDao = playerDao;
    this.floodgateApi = floodgateApi;

    this.errorOccurred = mAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.ERROR_OCCURRED);
  }

  @Subscribe(order = PostOrder.LATE)
  public void onPreLoginEvent(PreLoginEvent event) {
    if (!event.getResult().isAllowed()) {
      return;
    }

    try {
      String username = event.getUsername();
      if (!event.getResult().isForceOfflineMode()) {
        if (this.plugin.isPremium(username)) {
          event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());

          try {
            if (!Settings.IMP.MAIN.ONLINE_MODE_NEED_AUTH_STRICT) {
              CachedPremiumUser premiumUser = this.plugin.getPremiumCache(username);
              MinecraftConnection connection = this.getConnection(event.getConnection());
              if (!connection.isClosed() && premiumUser != null && !premiumUser.isForcePremium()
                  && this.plugin.isPremiumInternal(username.toLowerCase(Locale.ROOT)).getState() == PremiumState.UNKNOWN) {
                this.plugin.getPendingLogins().add(username);

                connection.getChannel().closeFuture().addListener(future -> {
                  if (this.plugin.getPendingLogins().remove(username)) {
                    this.plugin.setPremiumCacheLowercased(username.toLowerCase(Locale.ROOT), false);
                  }
                });
              }
            }
          } catch (Throwable throwable) {
            throw new IllegalStateException("failed to track authentication process", throwable);
          }
        } else {
          event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
        }
      } else {
        try {
          MinecraftConnection connection = this.getConnection(event.getConnection());
          if (!connection.isClosed()) {
            this.plugin.saveForceOfflineMode(username);

            connection.getChannel().closeFuture().addListener(future -> {
              this.plugin.unsetForcedPreviously(username);
            });
          }
        } catch (Throwable throwable) {
          throw new IllegalStateException("failed to track client disconnection", throwable);
        }
      }
    } catch (Throwable throwable) {
      event.setResult(PreLoginComponentResult.denied(this.errorOccurred));
      throw throwable;
    }
  }

  private MinecraftConnection getConnection(InboundConnection inbound) throws Throwable {
    LoginInboundConnection inboundConnection = (LoginInboundConnection) inbound;
    InitialInboundConnection initialInbound = (InitialInboundConnection) DELEGATE_FIELD.invokeExact(inboundConnection);
    return initialInbound.getConnection();
  }

  /*
  private boolean isPremiumByIdentifiedKey(InboundConnection inbound) throws Throwable {
    LoginInboundConnection inboundConnection = (LoginInboundConnection) inbound;
    InitialInboundConnection initialInbound = (InitialInboundConnection) DELEGATE_FIELD.invokeExact(inboundConnection);
    MinecraftConnection connection = initialInbound.getConnection();
    InitialLoginSessionHandler handler = (InitialLoginSessionHandler) connection.getSessionHandler();

    ServerLogin packet = (ServerLogin) LOGIN_FIELD.invokeExact(handler);
    if (packet == null) {
      return false;
    }

    UUID holder = packet.getHolderUuid();
    if (holder == null) {
      return false;
    }

    return holder.version() != 3;
  }
  */

  @Subscribe
  public void onPostLogin(PostLoginEvent event) {
    UUID uuid = event.getPlayer().getUniqueId();
    Runnable postLoginTask = this.plugin.getPostLoginTasks().remove(uuid);
    if (postLoginTask != null) {
      this.plugin.getServer().getScheduler()
          .buildTask(this.plugin, postLoginTask)
          .delay(Settings.IMP.MAIN.PREMIUM_AND_FLOODGATE_MESSAGES_DELAY, TimeUnit.MILLISECONDS)
          .schedule();
    }
  }

  @Subscribe
  public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
    if (event.getPlayer().isOnlineMode()) {
      CachedPremiumUser premiumUser = this.plugin.getPremiumCache(event.getPlayer().getUsername());
      if (premiumUser != null) {
        premiumUser.setForcePremium(true);
      }

      this.plugin.getPendingLogins().remove(event.getPlayer().getUsername());
    }

    if (this.plugin.needAuth(event.getPlayer())) {
      event.addOnJoinCallback(() -> this.plugin.authPlayer(event.getPlayer()));
    }
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onGameProfileRequest(GameProfileRequestEvent event) {
    if (Settings.IMP.MAIN.SAVE_UUID && (this.floodgateApi == null || !this.floodgateApi.isFloodgatePlayer(event.getOriginalProfile().getId()))) {
      RegisteredPlayer registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, event.getOriginalProfile().getId());

      if (registeredPlayer != null && !registeredPlayer.getUuid().isEmpty()) {
        event.setGameProfile(event.getOriginalProfile().withId(UUID.fromString(registeredPlayer.getUuid())));
        return;
      }
      registeredPlayer = AuthSessionHandler.fetchInfo(this.playerDao, event.getUsername());

      if (registeredPlayer != null) {
        String currentUuid = registeredPlayer.getUuid();

        if (currentUuid.isEmpty()) {
          try {
            registeredPlayer.setUuid(event.getGameProfile().getId().toString());
            this.playerDao.update(registeredPlayer);
          } catch (SQLException e) {
            throw new SQLRuntimeException(e);
          }
        } else {
          event.setGameProfile(event.getOriginalProfile().withId(UUID.fromString(currentUuid)));
        }
      }
    } else if (event.isOnlineMode()) {
      try {
        UpdateBuilder<RegisteredPlayer, String> updateBuilder = this.playerDao.updateBuilder();
        updateBuilder.where().eq(RegisteredPlayer.LOWERCASE_NICKNAME_FIELD, event.getUsername().toLowerCase(Locale.ROOT));
        updateBuilder.updateColumnValue(RegisteredPlayer.HASH_FIELD, "");
        updateBuilder.update();
      } catch (SQLException e) {
        throw new SQLRuntimeException(e);
      }
    }

    if (Settings.IMP.MAIN.FORCE_OFFLINE_UUID) {
      event.setGameProfile(event.getOriginalProfile().withId(UuidUtils.generateOfflinePlayerUuid(event.getUsername())));
    }

    if (!event.isOnlineMode() && !Settings.IMP.MAIN.OFFLINE_MODE_PREFIX.isEmpty()) {
      event.setGameProfile(event.getOriginalProfile().withName(Settings.IMP.MAIN.OFFLINE_MODE_PREFIX + event.getUsername()));
    }

    if (event.isOnlineMode() && !Settings.IMP.MAIN.ONLINE_MODE_PREFIX.isEmpty()) {
      event.setGameProfile(event.getOriginalProfile().withName(Settings.IMP.MAIN.ONLINE_MODE_PREFIX + event.getUsername()));
    }
  }

  static {
    try {
      DELEGATE_FIELD = MethodHandles.privateLookupIn(LoginInboundConnection.class, MethodHandles.lookup())
          .findGetter(LoginInboundConnection.class, "delegate", InitialInboundConnection.class);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new ReflectionException(e);
    }
  }
}
