package io.github.sinri.keel.test.lab.helper;

import io.github.sinri.keel.facade.KeelInstance;
import io.github.sinri.keel.helper.KeelDigestHelper;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;

public class KeelDigestHelperTest extends KeelTest {

    @TestUnit(skip = true)
    public Future<Void> testMD5() {
        KeelDigestHelperTest.main(null);
        return Future.succeededFuture();
    }

    public static void main(String[] args) {
        KeelDigestHelper helper = KeelInstance.KeelHelpers.digestHelper();
        assert "b70a23988f01287eb719d2b5747677d1".equals(helper.md5("124ABC哈哈😂"));
        assert "B70A23988F01287EB719D2B5747677D1".equals(helper.MD5("124ABC哈哈😂"));
    }
}
