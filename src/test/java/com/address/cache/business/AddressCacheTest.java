package com.address.cache.business;

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static java.net.InetAddress.getByName;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;


/**
 * Unit test for simple AddressCache.
 */
public class AddressCacheTest {

    private AddressCache addressCache;


    @Before
    public void setUp() {
        addressCache = new AddressCache(10000, MILLISECONDS);
    }

    @Test
    public void shouldReturnTrueWhenAddingAValidInetAddress()  throws UnknownHostException {
        assertTrue(addressCache.add(getByName("100.100.100.100")));
    }

    @Test
    public void shouldReturnFalseWhenAddingAInValidInetAddress() throws UnknownHostException {
        assertFalse(addressCache.add(null));
    }


    @Test
    public void shouldAddUniqueAddress() throws UnknownHostException, InterruptedException {
        InetAddress address = getByName("100.100.100.100");
        InetAddress address2 = getByName("100.100.100.101");

        addressCache.add(address);
        addressCache.add(address);
        addressCache.add(address);
        addressCache.add(address2);

        assertEquals(address, addressCache.take());
        assertEquals(address2, addressCache.take());
    }

    @Test
    public void shouldReturnNullWhenThereIsntAnyElementCached() {
        assertNull(addressCache.peek());
    }

    @Test
    public void shouldReturnLastElementCachedWhenCallingPeak() throws UnknownHostException {
        InetAddress address = getByName("100.100.100.100");

        addressCache.add(address);
        assertEquals(address, addressCache.peek());
        assertEquals(address, addressCache.peek());
    }

    @Test
    public void shouldNotRemoveNullAddress()  throws UnknownHostException {
        assertFalse(addressCache.remove(null));
    }

    @Test
    public void shouldNotRemoveElementIfCacheIsEmpty()  throws UnknownHostException {
        assertFalse(addressCache.remove(getByName("100.100.100.100")));
    }

    @Test
    public void shouldNotRemoveElementThatWasNotCached()  throws UnknownHostException {
        InetAddress address = getByName("100.100.100.100");
        addressCache.add(address);
        assertFalse(addressCache.remove(getByName("100.100.100.101")));
        assertEquals(address, addressCache.peek());
    }

    @Test
    public void shouldRemoveElementFromCache()  throws UnknownHostException, InterruptedException {
        InetAddress address = getByName("100.100.100.100");
        InetAddress address2 = getByName("100.100.100.101");
        InetAddress address3 = getByName("100.100.100.102");
        InetAddress address4 = getByName("100.100.100.103");
        InetAddress address5 = getByName("100.100.100.104");

        addressCache.add(address2);
        addressCache.add(address3);
        addressCache.add(address);
        addressCache.add(address4);
        addressCache.add(address5);
        assertTrue(addressCache.remove(address));
        assertFalse(addressCache.remove(address));
        assertEquals(address2, addressCache.take());
        assertEquals(address3, addressCache.take());
        assertEquals(address4, addressCache.take());
    }

    @Test
    public void shouldRemoveElementsFromCacheWhenExpireTheAge()  throws UnknownHostException, InterruptedException {
        AddressCache addressCache = new AddressCache(30, MILLISECONDS);
        InetAddress address = getByName("100.100.100.100");
        InetAddress address2 = getByName("100.100.100.101");
        InetAddress address3 = getByName("100.100.100.102");
        InetAddress address4 = getByName("100.100.100.103");
        InetAddress address5 = getByName("100.100.100.104");

        addressCache.add(address);
        addressCache.add(address2);
        addressCache.add(address3);
        addressCache.add(address4);
        addressCache.add(address5);
        assertEquals(address, addressCache.take());
        assertEquals(address2, addressCache.take());
        assertEquals(address3, addressCache.take());
        assertEquals(address4, addressCache.take());

        MILLISECONDS.sleep(500);
        assertNull(addressCache.peek());
    }

    @Test
    public void shouldReturnAddressOnlyAfterTheCacheHAsElement() throws UnknownHostException, InterruptedException{
        TimeUnit unit = mock(TimeUnit.class);
        AddressCache addressCache = new AddressCache(10000, unit);

        InetAddress address = getByName("100.100.100.100");
        Runnable run = () -> {
            try {
                unit.sleep(200);
                addressCache.add(address);
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        };
        Thread thread = new Thread(run);
        thread.start();

        assertEquals(address, addressCache.take());
    }

    @Test()
    public void shouldStopCleaningCacheWhenInterruptedExceptionOccurs() throws UnknownHostException, InterruptedException{
        TimeUnit unit = mock(TimeUnit.class);
        doThrow(InterruptedException.class).when(unit).sleep(anyInt());
        AddressCache addressCache = new AddressCache(1, unit);

        InetAddress address = getByName("100.100.100.100");
        addressCache.add(address);
        MILLISECONDS.sleep(5);
        assertEquals(address, addressCache.peek());
    }

    @Test
    public void shouldAddRemovePeakAndTakeAddressesConcurrently() throws UnknownHostException, InterruptedException{
        addressCache = new AddressCache(10000, MILLISECONDS);
        Runnable run = () -> {
            try {
                for (int i = 0; i < 255; i++) {
                    addressCache.add(getByName("100.100.100."+i));
                }

            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        };

        Runnable runTake = () -> {
            try {
                for (int i = 1; i < 255; i++) {
                    addressCache.take();
                }

            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        };

        Runnable runPeak = () -> {
            try {
                for (int i = 1; i < 255; i++) {
                    addressCache.peek();
                }

            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        };

        Runnable runRemove = () -> {
            try {
                for (int i = 1; i < 255; i++) {
                    addressCache.remove(getByName("100.100.100."+i));
                }

            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        };

        Thread thread = new Thread(run);
        thread.start();

        Thread thread2 = new Thread(runTake);
        thread2.start();

        Thread thread3 = new Thread(runPeak);
        thread3.start();

        Thread thread4 = new Thread(runRemove);
        thread4.start();
    }
}