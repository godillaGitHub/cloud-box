package com.godilla.cloud.box.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;

public abstract class CommandReceiver {
    public enum State {
        IDLE, COMMAND_LENGTH, COMMAND, TOKEN_LENGTH, TOKEN
    }

    private State currentState = State.IDLE;
    private int commandTypeLength;
    private int receivedLength;
    private StringBuilder cmd;

    private StringBuilder token;

    public void startReceive() {
        currentState = State.TOKEN_LENGTH;
        cmd = new StringBuilder();
        token = new StringBuilder();
        receivedLength = 0;
        System.out.println("STATE: Start command receiving");
    }

    public void receive(ChannelHandlerContext ctx, ByteBuf buf, Runnable finishOperation) throws Exception {

        // 4 + token.length + 4 + filenameBytes.length

        if (currentState == State.TOKEN_LENGTH) {
            if (buf.readableBytes() >= 4) {
                commandTypeLength = buf.readInt();
                if (commandTypeLength > 0) {
                    System.out.println("STATE: Get token length - " + commandTypeLength);
                }
                currentState = State.TOKEN;
                token.setLength(0);
            }
        }

        if (currentState == State.TOKEN) {
            while (buf.readableBytes() > 0) {
                token.append((char) buf.readByte()); // todo а как же кириллица?
                receivedLength++;
                if (receivedLength == commandTypeLength) {
                    System.out.println("STATE: Token received - " + token.toString());
                    currentState = State.COMMAND_LENGTH;
                    receivedLength = 0;
                    break;
                }
            }
        }

        if (currentState == State.COMMAND_LENGTH) {
            if (buf.readableBytes() >= 4) {
                commandTypeLength = buf.readInt();
                System.out.println("STATE: Get command length - " + commandTypeLength);
                currentState = State.COMMAND;
                cmd.setLength(0);
            }
        }

        if (currentState == State.COMMAND) {
            while (buf.readableBytes() > 0) {
                cmd.append((char) buf.readByte()); // todo а как же кириллица?
                receivedLength++;
                if (receivedLength == commandTypeLength) {
                    System.out.println("STATE: Command name received - " + cmd.toString());
                    parseCommand(ctx, cmd.toString(), token.toString());
                    currentState = State.IDLE;
                    finishOperation.run();
                    return;
                }
            }
        }

    }

    public abstract void parseCommand(ChannelHandlerContext ctx, String cmd, String token) throws Exception;
}
