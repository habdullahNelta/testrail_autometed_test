package com.idera.testrail.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeTest extends Base {

    @Test
    void Smoke() {
        Assert.assertEquals(4, 2 * 2, "Should equal 4");
    }

    @Test
    void Smoke2() {
        Assert.assertEquals(5, 3 * 2, "Should equal 6");
    }

    @Test
    void Smoke3() {
        Assert.assertEquals(10, 2 * 5, "Should equal 10");
    }


}
