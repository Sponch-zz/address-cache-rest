package com.address.cache.model;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * CacheObject containing information about the address added in the cache and
 * the time that it was interted
 */
public class CacheObject {

    private final InetAddress address;
    private final Calendar timeLive;
    private final long maxAge;

    public CacheObject(InetAddress address, long maxAge, TimeUnit timeUnit) {
        this.address = address;
        this.maxAge = maxAge;
        this.timeLive= Calendar.getInstance();
        this.timeLive.setTimeInMillis(timeLive.getTimeInMillis() + timeUnit.toMillis(maxAge));
    }

    public InetAddress getAddress() {
        return address;
    }

    public Date getDate() {
        return timeLive.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheObject)) return false;

        CacheObject that = (CacheObject) o;

        return getAddress().equals(that.getAddress());

    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }
}