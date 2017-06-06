package com.wonhwee.empmgr;


import com.wonhwee.empmgr.dao.EmpMgrContract;

import org.junit.Test;

public class EmpMgrContractTest {
    @Test
    public void testAuthority(){
        String authority = EmpMgrContract.CONTENT_AUTHORITY;
        org.junit.Assert.assertEquals("Unexpected authority value",
                "com.wonhwee.empmgr.empmgrprovider", authority);

    }

    @Test
    public void testConcat(){
        EmpMgrContract contract = new EmpMgrContract();
        String content = contract.concatContent("com.wonhwee.empmgr");
        org.junit.Assert.assertEquals("content://com.wonhwee.empmgr", content);
    }
}
