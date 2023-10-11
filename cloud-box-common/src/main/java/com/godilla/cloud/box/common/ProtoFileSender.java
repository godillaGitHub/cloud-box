package com.godilla.cloud.box.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtoFileSender {
    public static void sendFile(Path path,
                                String token,
                                Channel channel,
                                ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        // 1 + 4 + token.length + 4 + filenameBytes.length + 8 ->  SIGNAL_BYTE  + TOKEN_LENGTH(int)  + TOKEN + FILENAME_LENGTH(int) + FILENAME + FILE_LENGTH(long)
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + + tokenBytes.length + 4 + filenameBytes.length + 8);
        buf.writeByte(CloudBoxCommandsList.FILE_SIGNAL_BYTE);
        buf.writeInt(tokenBytes.length);
        buf.writeBytes(tokenBytes);
        buf.writeInt(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

}

