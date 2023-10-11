package com.godilla.cloud.box.client;
import com.godilla.cloud.box.common.ProtoFileSender;
import com.godilla.cloud.box.common.ProtoCommandSender;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class ClientApp {

    private static String TOKEN = "@12121212@";

    public static void main(String[] args) throws Exception {

        CountDownLatch connectionOpened = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(connectionOpened)).start();
        connectionOpened.await();

        Scanner sc = new Scanner(System.in);
        while (true) {
            String cmd = sc.nextLine();
            if (cmd.equals("/exit")) {
                break;
            }
            if (cmd.startsWith("/send ")) {
                String filename = cmd.split("\\s")[1];
                Path filePath = Paths.get("client_repository", filename);
                if (!Files.exists(filePath)) {
                    System.out.println("Файл для отправки не найден в репозитории");
                    continue;
                }
                ProtoFileSender.sendFile(filePath, TOKEN + " ", Network.getInstance().getCurrentChannel(), future -> {
                    if (!future.isSuccess()) {
                        System.out.println("Не удалось отправить файл на сервер");
                        future.cause().printStackTrace();
                    }
                    if (future.isSuccess()) {
                        System.out.println("Файл успешно передан");
                    }
                });
                continue;
            }
            if (cmd.startsWith("/download ")) {
                String filename = cmd.split("\\s")[1];
                Path fileRegionPath = Paths.get("client_region", "forregion.txt");
                ProtoCommandSender.sendFileRequest(fileRegionPath,TOKEN + " ", "/request_download ", filename, Network.getInstance().getCurrentChannel(), future -> {
                    if (!future.isSuccess()) {
                        System.out.println("Не удалось получить файл с сервера");
                        future.cause().printStackTrace();
                    }
                    if (future.isSuccess()) {
                        System.out.println("Файл успешно получен");
                    }
                });
                continue;
            }
            if (cmd.startsWith("/rename ")) {
                String oldFilename = cmd.split("\\s")[1];
                String newFilename = cmd.split("\\s")[2];
                Path fileRegionPath = Paths.get("client_region", "forregion.txt");
                ProtoCommandSender.sendFileRequest(fileRegionPath, TOKEN + " ","/request_rename ", oldFilename + " " + newFilename, Network.getInstance().getCurrentChannel(),  future -> {
                    if (!future.isSuccess()) {
                        System.out.println("Не удалось переименовать файл на сервере");
                        future.cause().printStackTrace();
                    }
                    if (future.isSuccess()) {
                        System.out.println("Файл успешно переименован");
                    }
                });
                continue;
            }
            if (cmd.startsWith("/delete ")) {
                String filename = cmd.split("\\s")[1];
                Path fileRegionPath = Paths.get("client_region", "forregion.txt");
                ProtoCommandSender.sendFileRequest(fileRegionPath, TOKEN + " ", "/request_delete ", filename, Network.getInstance().getCurrentChannel(),  future -> {
                    if (!future.isSuccess()) {
                        System.out.println("Не удалось удалить файл на сервере");
                        future.cause().printStackTrace();
                    }
                    if (future.isSuccess()) {
                        System.out.println("Файл успешно удален");
                    }
                });
                continue;
            }
            System.out.println("Введена неверная команда, попробуйте снова");
        }
    }
}