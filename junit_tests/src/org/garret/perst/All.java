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
    org.garret.perst.StorageTestThreaded.class,
    org.garret.perst.TestAgg.class,
    org.garret.perst.TestAlloc.class,
    org.garret.perst.TestAutoIndices.class,
    org.garret.perst.TestBackup.class,
    org.garret.perst.TestBit.class,
    org.garret.perst.TestBitmap.class,
    org.garret.perst.TestBlob.class,
    org.garret.perst.TestBtreeCompoundIndex.class,
    org.garret.perst.TestCodeGenerator.class,
    org.garret.perst.TestCompoundIndex.class,
    org.garret.perst.TestConcur.class,
    org.garret.perst.TestDbServer.class,
    org.garret.perst.TestDecimal.class,
    org.garret.perst.TestDerivedIndex.class,
    org.garret.perst.TestDynamicObjects.class,
    org.garret.perst.TestFullTextIndex.class,
    org.garret.perst.TestGC.class,
    org.garret.perst.TestIndex.class,
    org.garret.perst.TestIndex2.class,
    org.garret.perst.TestJSQL.class,
    org.garret.perst.TestJSQLContains.class,
    org.garret.perst.TestJsqlJoin.class,
    org.garret.perst.TestKDTree.class,
    org.garret.perst.TestKDTree2.class,
    org.garret.perst.TestLeak.class,
    org.garret.perst.TestLink.class,
    org.garret.perst.TestList.class,
    org.garret.perst.TestLoad.class,
    org.garret.perst.TestMap.class,
    org.garret.perst.TestMaxOid.class,
    org.garret.perst.TestMod.class,
    org.garret.perst.TestPatricia.class,
    org.garret.perst.TestPerf.class,
    org.garret.perst.TestPersistentMap.class,
    org.garret.perst.TestRandomBlob.class,
    org.garret.perst.TestRaw.class,
    org.garret.perst.TestRecovery.class,
    org.garret.perst.TestRegex.class,
    org.garret.perst.TestRndIndex.class,
    org.garret.perst.TestRollback.class,
    org.garret.perst.TestRtree.class,
    org.garret.perst.TestServer.class,
    org.garret.perst.TestSet.class,
    org.garret.perst.TestStorage.class,
    org.garret.perst.TestThickIndex.class,
    org.garret.perst.TestVersion.class,
    org.garret.perst.TestWeakHashTable.class,
    org.garret.perst.TestXML.class
})
public class All
{
}
