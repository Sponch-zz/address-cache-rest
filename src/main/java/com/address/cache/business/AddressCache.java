package com.address.cache.business;


import com.address.cache.model.CacheObject;

import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
 * The AddressCache has a max age for the elements it's storing, an add method
 * for adding elements, a remove method for removing, a peek method which
 * returns the most recently added element, and a take method which removes
 * and returns the most recently added element.
 */


public class AddressCache {

    private static final Logger log = Logger.getLogger(AddressCache.class.getName());

    private final ConcurrentLinkedQueue<CacheObject> cache = new ConcurrentLinkedQueue<>();

    private final TimeUnit timeUnit;

    private final long maxAge;

    public AddressCache(long maxAge, TimeUnit timeUnit) {
        this.maxAge = maxAge;
        this.timeUnit = timeUnit;
        Runnable task = () -> {
            try {
                while (true) {
                    if (cache.size() != 0) {
                        Iterator<CacheObject> iterator = cache.iterator();
                        while (iterator.hasNext()) {
                            Date date = new Date();
                            CacheObject cache = iterator.next();
                            if (MILLISECONDS.convert(date.getTime() - cache.getDate().getTime(), timeUnit) > maxAge) {
                                iterator.remove();
                            } else {
                                break;
                            }
                        }
                    }
                    MILLISECONDS.sleep(50);
                }
            } catch (InterruptedException e) {
                log.warning("Thread Interrupted");
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    /**
     * add() method must store unique elements only (existing elements must be ignored).
     * This will return true if the element was successfully added.
     *
     * @param address InetAddress
     * @return boolean
     */
    public boolean add(InetAddress address) {
        if (address != null) {
            CacheObject cacheObject = new CacheObject(address, maxAge , timeUnit);
            if (!cache.contains(cacheObject)) {
                cache.add(cacheObject);
                return true;
            }
        }
        return false;
    }

    /**
     * remove() method will return true if the address was successfully removed
     *
     * @param address InetAddress
     * @return boolean
     */
    public boolean remove(InetAddress address) {
        if (address != null) {
            Iterator<CacheObject> iterator = cache.iterator();
            while (iterator.hasNext()) {
                if (address.equals(iterator.next().getAddress())) {
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The peek() method will return the most recently added element,
     * null if no element exists.
     *
     * @return InetAddress
     */
    public InetAddress peek() {
        return getInetAddress(false);
    }

    /**
     * take() method retrieves and removes the most recently added element
     * from the cache and waits if necessary until an element becomes available.
     *
     * @return InetAddress return of InetAddress
     */
    public InetAddress take() throws InterruptedException {
        while (cache.isEmpty()) {
            MILLISECONDS.sleep(50);
        }
        return getInetAddress(true);
    }

    /**
     * Get InetAddress from the cache
     *
     * @param remove if true, get and remove. Otherwise only get the object
     * @return InetAddress return of the address
     */
    private InetAddress getInetAddress(boolean remove) {
        Iterator<CacheObject> iterator = cache.iterator();
        if (iterator.hasNext()) {
            CacheObject cacheObject = iterator.next();
            if (remove) {
                iterator.remove();
            }
            return cacheObject.getAddress();
        }
        return null;
    }
}
