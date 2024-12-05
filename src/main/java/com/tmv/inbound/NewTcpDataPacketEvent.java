package com.tmv.inbound;

import com.tmv.inbound.teltonika.model.TcpDataPacket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class NewTcpDataPacketEvent extends ApplicationEvent {

    @Getter
    private final TcpDataPacket dataPacket;

    public NewTcpDataPacketEvent(Object source, TcpDataPacket tcpDataPacket) {
        super(source);
        this.dataPacket = tcpDataPacket;
    }

}