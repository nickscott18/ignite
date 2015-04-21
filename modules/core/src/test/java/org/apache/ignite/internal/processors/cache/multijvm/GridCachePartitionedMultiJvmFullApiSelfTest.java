/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.cache.multijvm;

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.processors.cache.distributed.near.*;
import org.apache.ignite.internal.processors.resource.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.lang.*;

import java.util.*;

/**
 * TODO: Add class description.
 */
public class GridCachePartitionedMultiJvmFullApiSelfTest extends GridCachePartitionedMultiNodeFullApiSelfTest {
    /** Local ignite. */
    private Ignite locIgnite;
    
    /** Proces name to process map. */
    private final Map<String, IgniteExProxy> ignites = new HashMap<>();

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        super.beforeTestsStarted();
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        for (IgniteExProxy ignite : ignites.values())
            ignite.getProcess().kill();

        ignites.clear();
        
        locIgnite = null;

        super.afterTestsStopped();
    }

    @Override protected void beforeTest() throws Exception {
        super.beforeTest(); // TODO: CODE: implement.
    }

    @Override protected void afterTest() throws Exception {
        for (IgniteExProxy ignite : ignites.values())
            ignite.getProcess().kill();
        
        super.afterTest(); // TODO: CODE: implement.
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
//        IgniteConfiguration cfg = super.getConfiguration(gridName);
//
//        ((TcpDiscoverySpi)cfg.getDiscoverySpi()).setIpFinder(IgniteNodeRunner.ipFinder);
//
//        return cfg;
        
        return IgniteNodeRunner.theSameConf(gridName);
    }

    /** {@inheritDoc} */
    @Override protected CacheConfiguration cacheConfiguration(String gridName) throws Exception {
        return super.cacheConfiguration(gridName); // TODO: CODE: implement.
    }

    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return 2;
    }

    /** {@inheritDoc} */
    protected boolean isMultiJvm() {
        return true;
    }

    /** {@inheritDoc} */
    protected Ignite startGrid(String gridName, GridSpringResourceContext ctx) throws Exception {
        if (!isMultiJvm() || gridName.endsWith("0")) {
            locIgnite = super.startGrid(gridName, ctx);

            return locIgnite;
        }

        startingGrid.set(gridName);

        try {
            IgniteConfiguration cfg = optimize(getConfiguration(gridName));

            IgniteExProxy proxy = new IgniteExProxy(cfg, log, locIgnite);

            ignites.put(gridName, proxy);

            return proxy;
        }
        finally {
            startingGrid.set(null);
        }
    }

    /** {@inheritDoc} */
    @Override protected IgniteEx grid(int idx) {
        if (!isMultiJvm() || idx == 0)
            return super.grid(idx);

        String name = getTestGridName(idx);

        return ignites.get(name);
    }

    /**
     * @throws Exception If failed.
     */
    public void testPutAllRemoveAll() throws Exception {
//        for (int i = 0; i < gridCount(); i++) {
//            IgniteEx grid0 = grid0(i);
//
//            info(">>>>> Grid" + i + ": " + grid0.localNode().id());
//        }

        Map<Integer, Integer> putMap = new LinkedHashMap<>();

        int size = 100;

        for (int i = 0; i < size; i++)
            putMap.put(i, i * i);

        IgniteEx grid0 = grid(0);

        IgniteCache<Object, Object> c0 = grid0.cache(null);

        IgniteEx grid1 = grid(1);

        IgniteCache<Object, Object> c1 = grid1.cache(null);

//        c0.putAll(putMap);
//
//        atomicClockModeDelay(c0);
//
//        c1.removeAll(putMap.keySet());
//
//        for (int i = 0; i < size; i++) {
//            assertNull(c0.get(i));
//            assertNull(c1.get(i));
//        }
//
//        for (IgniteExProxy ignite : ignites.values())
//            ignite.getProcess().kill();
    }

    /**
     * @throws Exception If failed.
     */
    public void testBroadcast() throws Exception {
        IgniteEx grid0 = grid(0);
        IgniteEx grid1 = grid(1);

        Thread.sleep(10_000);

//        ClusterGroup grp = grid0.cluster().forNode(grid1.localNode());

        grid0.compute().broadcast(new IgniteRunnable() {
            @Override public void run() {
                System.out.println(">>>>> trololo");
            }
        });
        
        Thread.sleep(10_000);
    }

    /**
     * @throws Exception If failed.
     */
    public void testSimpleBroadcast_worked() throws Exception {
        Ignite rmt = null;

        try(Ignite ignite = Ignition.start(IgniteNodeRunner.theSameConf("SomeLocGrid"))) {
            Thread.sleep(5_000);

            rmt = new IgniteExProxy(IgniteNodeRunner.theSameConf("remoteNode"), ignite.log(), ignite);

            Thread.sleep(5_000);

            ignite.compute().broadcast(new IgniteRunnable() {
                @Override public void run() {
                    System.out.println(">>>>> trololo");
                }
            });

            Collection<String> res = ignite.compute().broadcast(new C1<Integer, String>() {
                @Override public String apply(Integer integer) {
                    return String.valueOf(integer + 12);
                }
            }, 100500);

            System.out.println(">>>>> " + res);
        }
        finally {
            rmt.close();
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testRmt2RmtBroadcast_worked() throws Exception {
        Ignite rmt1 = null;
        Ignite rmt2 = null;

        try {
            rmt1 = new IgniteExProxy(IgniteNodeRunner.theSameConf("remoteNode1"), log, null);

            Thread.sleep(5_000);

            rmt2 = new IgniteExProxy(IgniteNodeRunner.theSameConf("remoteNode2"), log, null);

            Thread.sleep(5_000);
        }
        finally {
            rmt1.close();
            rmt2.close();
        }
    }
}
