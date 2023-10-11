package com.godilla.cloud.box.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtoCommandSender {
    public static void sendFileRequest(Path path,
                                       String token,
                                       String commandName,
                                       String filename,
                                       Channel outChannel,
                                       ChannelFutureListener finishListener) throws IOException {

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        byte[] filenameBytes = (commandName + filename).getBytes(StandardCharsets.UTF_8);

        // 1 + 4 + token.length + 4 + filenameBytes.length

        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + tokenBytes.length + 4 + filenameBytes.length);
        buf.writeByte(CloudBoxCommandsList.CMD_SIGNAL_BYTE);
        buf.writeInt(tokenBytes.length);
        buf.writeBytes(tokenBytes);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        outChannel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = outChannel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}