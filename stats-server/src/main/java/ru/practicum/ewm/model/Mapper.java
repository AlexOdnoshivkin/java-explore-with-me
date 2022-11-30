package ru.practicum.ewm.model;

public class Mapper {
    private Mapper() {
    }

    public static Stat toStatFromEndpointHit(EndpointHit endpointHit) {
        Stat stat = new Stat();
        stat.setIdentificationRecord(endpointHit.getId());
        stat.setApp(endpointHit.getApp());
        stat.setUri(endpointHit.getUri());
        stat.setTimesTamp(endpointHit.getTimesTamp());
        stat.setIp(endpointHit.getIp());
        return stat;
    }
}
