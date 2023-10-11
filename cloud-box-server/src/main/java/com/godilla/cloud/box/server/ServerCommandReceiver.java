package com.godilla.cloud.box.server;

import com.godilla.cloud.box.common.CommandReceiver;
import com.godilla.cloud.box.common.ProtoCommandSender;
import com.godilla.cloud.box.common.ProtoFileSender;
import io.netty.channel.ChannelHandlerContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ServerCommandReceiver extends CommandReceiver {

    private static String TOKEN = "@12121212@";
    private static String TOKEN_EMPTY = "@@@@@@@@@";

    @Override
    public void parseCommand(ChannelHandlerContext ctx, String cmd, String token) throws Exception {

        if (cmd.startsWith("/request_download ")) {
            if (token.split("\\s")[0].equals(TOKEN)) {
                //Проверка токена пройдена
                String fileToClientName = cmd.split("\\s")[1];
                Path filePath = Paths.get("server_repository", fileToClientName);
                if (Files.exists(filePath)) {
                    //Файл для отправки найден в репозитории
                    ProtoFileSender.sendFile(Paths.get("server_repository", fileToClientName), TOKEN_EMPTY.toString(), ctx.channel(), null);
                }
            }
        }
        if (cmd.startsWith("/request_rename ")) {
            if (token.split("\\s")[0].equals(TOKEN)) {
                //Проверка токена пройдена
                String oldFileToClientName = cmd.split("\\s")[1];
                Path oldFilePath = Paths.get("server_repository", oldFileToClientName);
                if (Files.exists(oldFilePath)) {
                    //Файл для отправки найден в репозитории
                    String newFileToClientName = cmd.split("\\s")[2];
                    Path newFilePath = Paths.get("server_repository", newFileToClientName);
                    if (oldFilePath.toFile().renameTo(newFilePath.toFile())) {
                        ProtoCommandSender.sendFileRequest(newFilePath, TOKEN_EMPTY.toString(), "/response_rename ", newFileToClientName, ctx.channel(), null);
                    }
                }
            }
        }
        if (cmd.startsWith("/request_delete ")) {
            if (token.split("\\s")[0].equals(TOKEN)) {
                //Проверка токена пройдена
                String fileToClientName = cmd.split("\\s")[1];
                Path filePath = Paths.get("server_repository", fileToClientName);
                if (Files.exists(filePath)) {
                    //Файл для отправки найден в репозитории
                    if (filePath.toFile().delete()) {
                        Path fileRegionPath = Paths.get("server_region", "forregion.txt");
                        ProtoCommandSender.sendFileRequest(fileRegionPath, TOKEN_EMPTY.toString(), "/response_delete ", fileToClientName, ctx.channel(), null);
                    }
                }
            }
        }
    }
}
