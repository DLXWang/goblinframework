package org.goblinframework.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.goblinframework.core.serialization.Serializer;
import org.goblinframework.core.serialization.SerializerManager;
import org.goblinframework.core.transcoder.TranscoderSetting;
import org.goblinframework.transport.exception.GoblinTransportCodecException;

@ChannelHandler.Sharable
public class TransportMessageEncoder extends MessageToByteEncoder<TransportMessage> {

  private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

  private static final TransportMessageEncoder instance = new TransportMessageEncoder();

  public static TransportMessageEncoder getInstance() {
    return instance;
  }

  private TransportMessageEncoder() {
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, TransportMessage msg, ByteBuf out) throws Exception {
    if (msg.message == null) {
      throw new GoblinTransportCodecException("Encoding null message not allowed");
    }
    Serializer serializer = null;
    if (msg.serializer != 0) {
      serializer = SerializerManager.INSTANCE.getSerializer(msg.serializer);
      if (serializer == null) {
        throw new GoblinTransportCodecException("Serializer [" + msg.serializer + "] not found");
      }
    }
    int startIdx = out.writerIndex();
    ByteBufOutputStream bos = new ByteBufOutputStream(out);
    bos.write(LENGTH_PLACEHOLDER);
    TranscoderSetting.builder().serializer(serializer).build().transcoder().encode(bos, msg.message);
    bos.flush();
    bos.close();
    int endIdx = out.writerIndex();
    out.setInt(startIdx, endIdx - startIdx - LENGTH_PLACEHOLDER.length);
  }
}
