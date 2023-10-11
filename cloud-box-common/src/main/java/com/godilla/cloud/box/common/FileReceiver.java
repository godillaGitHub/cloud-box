package com.godilla.cloud.box.common;

import com.godilla.cloud.box.common.CloudBoxCommandsList;
import com.godilla.cloud.box.common.ProtoFileSender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;

public class FileReceiver {
    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE, TOKEN_LENGTH, TOKEN
    }

    private String rootDir;
    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    public FileReceiver(String rootDir) {
        this.rootDir = rootDir;
    }

    public void startReceive() {
        currentState = State.TOKEN_LENGTH;
        receivedFileLength = 0L;
        System.out.println("STATE: Start file receiving");
    }

    public void receive(ChannelHandlerContext ctx, ByteBuf buf, Runnable finishOperation) throws Exception {

        // 4 + token.length + 4 + filenameBytes.length + 8

        if (currentState == State.TOKEN_LENGTH) {
            if (buf.readableBytes() >= 4) {
                nextLength = buf.readInt();
                if (nextLength > 0) {
                    System.out.println("STATE: Get token length - " + nextLength);
                }
                currentState = State.TOKEN;
            }
        }

        if (currentState == State.TOKEN) {
            if (buf.readableBytes() >= nextLength) {
                byte[] token = new byte[nextLength];
                buf.readBytes(token);
                if (nextLength > 0) {
                    System.out.println("STATE: Token received - " + new String(token, "UTF-8"));
                }
                currentState = State.NAME_LENGTH;
            }
        }

        if (currentState == State.NAME_LENGTH) {
            if (buf.readableBytes() >= 4) {
                nextLength = buf.readInt();
                System.out.println("STATE: Get filename length - " + nextLength);
                currentState = State.NAME;
            }
        }

        if (currentState == State.NAME) {
            if (buf.readableBytes() >= nextLength) {
                byte[] fileName = new byte[nextLength];
                buf.readBytes(fileName);
                System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                out = new BufferedOutputStream(new FileOutputStream(rootDir + "/" + new String(fileName)));
                currentState = State.FILE_LENGTH;
            }
        }

        if (currentState == State.FILE_LENGTH) {
            if (buf.readableBytes() >= 8) {
                fileLength = buf.readLong();
                System.out.println("STATE: File length received - " + fileLength);
                currentState = State.FILE;
            }
        }

        if (currentState == State.FILE) {
            while (buf.readableBytes() > 0) {
                out.write(buf.readByte());
                receivedFileLength++;
                if (fileLength == receivedFileLength) {
                    currentState = State.IDLE;
                    System.out.println("File received");
                    out.close();
                    finishOperation.run();
                    return;
                }
            }
        }
    }
}
