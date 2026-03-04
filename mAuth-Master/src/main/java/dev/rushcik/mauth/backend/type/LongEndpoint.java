package dev.rushcik.mauth.backend.type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import java.util.function.Function;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.backend.Endpoint;

public class LongEndpoint extends Endpoint {

  private Function<String, Long> function;
  private long value;

  public LongEndpoint(mAuth plugin, String type, Function<String, Long> function) {
    super(plugin, type, null);
    this.function = function;
  }

  public LongEndpoint(mAuth plugin, String type, String username, long value) {
    super(plugin, type, username);
    this.value = value;
  }

  @Override
  public void writeContents(ByteArrayDataOutput output) {
    output.writeLong(this.value);
  }

  @Override
  public void readContents(ByteArrayDataInput input) {
    this.value = this.function.apply(this.username);
  }

  @Override
  public String toString() {
    return "LongEndpoint{"
        + "username='" + this.username + '\''
        + ", value=" + this.value
        + '}';
  }
}
