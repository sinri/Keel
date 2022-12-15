package io.github.sinri.keel.test.core;

import io.github.sinri.keel.lagecy.Keel;
import io.vertx.core.json.JsonObject;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

public class GCTest {
    public static void main(String[] args) {
        //ArrayList<Long> list=new ArrayList<>();
        double sum = 0;
        for (long i = 0; i < 100000; i++) {
            //list.add(i);
            ArrayList<Object> objects = new ArrayList<>();
            objects.add(i);
            sum += i;
        }

        for (var x : ManagementFactory.getGarbageCollectorMXBeans()) {
            String name = x.getName();
            String[] memoryPoolNames = x.getMemoryPoolNames();
            ObjectName objectName = x.getObjectName();

            long collectionCount = x.getCollectionCount();
            long collectionTime = x.getCollectionTime();

            JsonObject j = new JsonObject();
            j
                    .put("class", x.getClass().getName())
                    .put("name", name)
                    .put("memoryPoolNames", Keel.helpers().string().joinStringArray(memoryPoolNames, ","))
                    .put("objectName", objectName)
                    .put("collectionCount", collectionCount)
                    .put("collectionTime", collectionTime);

            System.out.println("ONE GC: " + j.encodePrettily());
        }
    }

}
