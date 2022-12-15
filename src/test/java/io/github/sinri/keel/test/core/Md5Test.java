package io.github.sinri.keel.test.core;

import io.github.sinri.keel.lagecy.Keel;

public class Md5Test {
    public static void main(String[] args) {
        //com.leqee.oc.tachiba.catholic.service.v1.QueryUserListService||["hlshen"]|||["NORMAL"]
        String x1 = "com.leqee.oc.tachiba.catholic.service.v1.QueryUserListService||[\"hlshen\"]|||[\"NORMAL\"]";
        //com.leqee.oc.tachiba.catholic.service.v1.QueryUserListService||["hlye2"]|||["NORMAL"]
        String x2 = "com.leqee.oc.tachiba.catholic.service.v1.QueryUserListService||[\"hlye2\"]|||[\"NORMAL\"]";
        var y1 = Keel.helpers().digest().md5(x1);
        var y2 = Keel.helpers().digest().md5(x2);
        System.out.println("y1: " + y1 + " x1: " + x1);
        System.out.println("y2: " + y2 + " x2: " + x2);
    }
}
