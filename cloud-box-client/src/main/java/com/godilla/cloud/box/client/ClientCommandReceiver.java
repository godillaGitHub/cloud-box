package com.godilla.cloud.box.client;

import com.godilla.cloud.box.common.CommandReceiver;
import com.godilla.cloud.box.common.ProtoFileSender;
import io.netty.channel.ChannelHandlerContext;

import javax.naming.OperationNotSupportedException;
import java.nio.file.Paths;

public class ClientCommandReceiver extends CommandReceiver {
    @Override
    public void parseCommand(ChannelHandlerContext ctx, String cmd, String token) throws Exception {
        if (cmd.startsWith("/response_rename ") || cmd.startsWith("/response_delete ")) {
            System.out.println("Получено подтверждение команды от сервера.");
        }
        else {
            throw new OperationNotSupportedException("Мы не должны сюда попадать на клиенте");
        }
    }
}
