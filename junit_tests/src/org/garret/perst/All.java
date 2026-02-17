/*
 * $URL: All.java $ 
 * $Rev: 3582 $ 
 * $Date: 2007-11-25 14:29:06 +0300 (Вс., 25 нояб. 2007) $
 *
 * Copyright 2005 Netup, Inc. All rights reserved.
 * URL:    http://www.netup.biz
 * e-mail: info@netup.biz
 */

package org.garret.perst;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

 /**
 * <p>
 *   Collect all the PERST tests into one suite.
 * </p>
 */ 
@Suite
@SelectClasses({
    org.garret.perst.BlobTest.class,
    org.garret.perst.DatabaseTest.class,
    org.garret.perst.PersistentSetTest.class,
    org.garret.perst.QueryTest.class,
    org.garret.perst.StorageFactoryTest.class,
    org.garret.perst.StorageTest.class,
    org.garret.perst.StorageTestThreaded.class
})
public class All
{
}
